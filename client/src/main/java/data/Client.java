package data;

import java.io.FileInputStream;
import java.net.*;

import java.security.*;

import java.util.List;


import Security.Security;


import org.glassfish.jersey.client.ClientConfig;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import Security.InsecureHostNameVerifier;

import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class Client {

    private final URI serverURI;
    private final javax.ws.rs.client.Client client;

    private final KeyPair keyPair;
    public Client(URI proxyURI, KeyPair keyPair) {
        this.serverURI = proxyURI;
        this.keyPair = keyPair;
        this.client = startClient();
    }
    private SSLContext getContext() throws Exception {
        KeyStore ts = KeyStore.getInstance("JKS");

        try (FileInputStream fis = new FileInputStream("security/clientcacerts.jks")) {
            ts.load(fis, "password".toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);

        String protocol = "TLSv1.3";
        SSLContext sslContext = SSLContext.getInstance(protocol);

        sslContext.init(null, tmf.getTrustManagers(), null);

        return sslContext;
    }


    private javax.ws.rs.client.Client startClient() {

        try {
            SSLContext context = getContext();
            HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostNameVerifier());
            HttpsURLConnection.setDefaultSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
            ClientConfig config = new ClientConfig();
            return ClientBuilder.newBuilder().sslContext(context)
                    .withConfig(config).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public KeyPair getKeyPair() {
        return this.keyPair;
    }

    public void executeCommand(Request request) {
        switch (request.getRequestType()) {
            case CREATE_ACCOUNT:
                createAccount(request);
                break;
            case GET_BALANCE:
                getBalance(request);
                break;
            case LOAD_MONEY:
                loadMoney(request);
                break;
            case GET_EXTRACT:
                getExtract(request);
                break;
            case GET_TOTAL_VALUE:
                getTotalValue(request);
                break;
            case GET_GLOBAL_VALUE:
                getGlobalValue(request);
                break;
            case SEND_TRANSACTION:
                sendTransaction(request);
                break;
            case GET_LEDGER:
                getLedger(new Request(LedgerRequestType.GET_LEDGER));
                break;
            default:
                break;
        }
    }


    private void getLedger(Request request) {

        request.setPublicKey(this.keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        javax.ws.rs.client.Client client = startClient();
        WebTarget target = client.target( serverURI ).path("ledger");
        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                List<Transaction> ledger = reply.getListReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException pe ) {
            pe.printStackTrace();
            throw new InternalServerErrorException();
        }

    }

    private void sendTransaction(Request request) {

        request.setPublicKey(this.keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        javax.ws.rs.client.Client client = startClient();

        WebTarget target = client.target( serverURI ).path("transaction");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if(  r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKey(), request.getRequestType().toString().getBytes(), reply.getSignature()))
                    throw new InternalServerErrorException();
            } else
                throw new WebApplicationException(r.getStatus());

        } catch ( ProcessingException pe ) {
            pe.printStackTrace();
            throw new InternalServerErrorException();
        }
    }

    private void getGlobalValue(Request request) {
        request.setPublicKey(this.keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        WebTarget target = client.target( serverURI ).path("value");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKey(), request.getRequestType().toString().getBytes(), reply.getSignature()))
                    throw new InternalServerErrorException();
            } else
                throw new WebApplicationException(r.getStatus());

        } catch ( ProcessingException pe ) {
            pe.printStackTrace(); //
            throw new InternalServerErrorException();
        }
    }

    private void getTotalValue(Request request) {
        request.setPublicKey(this.keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        WebTarget target = client.target( serverURI ).path("accounts/value");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKey(), request.getRequestType().toString().getBytes(), reply.getSignature()))
                    throw new InternalServerErrorException();
                int value = reply.getIntReply();
            } else
                throw new WebApplicationException(r.getStatus());

        } catch (ProcessingException pe ) {
            pe.printStackTrace(); //
            throw new InternalServerErrorException();
        }
    }

    private void getExtract(Request request) {
        request.setPublicKey(this.keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        WebTarget target = client.target( serverURI ).path("account/extract");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKey(), request.getRequestType().toString().getBytes(), reply.getSignature()))
                    throw new InternalServerErrorException();
            } else
                throw new WebApplicationException(r.getStatus());

        } catch (ProcessingException pe ) {
            pe.printStackTrace();
            throw new InternalServerErrorException();
        }
    }

    private void loadMoney(Request request) {
        request.setPublicKey(this.keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        WebTarget target = client.target( serverURI ).path("account/load");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if(  r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKey(), request.getRequestType().toString().getBytes(), reply.getSignature()))
                    throw new InternalServerErrorException();
            } else
                throw new WebApplicationException(r.getStatus());

        } catch (ProcessingException pe) {
            pe.printStackTrace();
            throw new InternalServerErrorException();
        }
    }

    private void getBalance(Request request) {

        request.setPublicKey(this.keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        WebTarget target = client.target( serverURI ).path("account/balance");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKey(), request.getRequestType().toString().getBytes(), reply.getSignature()))
                    throw new InternalServerErrorException();
            } else
                throw new WebApplicationException(r.getStatus());

        } catch (ProcessingException pe ) {
            pe.printStackTrace();
            throw new InternalServerErrorException();
        }
    }

    private void createAccount(Request request) {

        request.setPublicKey(this.keyPair.getPublic().getEncoded());

        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        WebTarget target = client.target( serverURI ).path("account");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));

            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKey(), request.getRequestType().toString().getBytes(), reply.getSignature()))
                    throw new InternalServerErrorException();
            } else
                throw new WebApplicationException(r.getStatus());

        } catch (ProcessingException pe) { //Error in communication with server
            pe.printStackTrace();
            throw new InternalServerErrorException();
        }

    }
}

