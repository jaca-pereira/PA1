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

    private final byte[] account;
    public Client(URI proxyURI, KeyPair keyPair) {
        this.serverURI = proxyURI;
        this.keyPair = keyPair;
        this.account = this.idMaker(keyPair);
        this.client = startClient();
    }

    private byte[] idMaker(KeyPair keyPair) {
        return null;
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

    public byte[] getAccount() {
        return this.account;
    }

    public void getLedger() {
        Request request = new Request(LedgerRequestType.GET_LEDGER);
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

    public void sendTransaction(byte[] accountDestiny) {
        SecureRandom nonce = new SecureRandom();
        Request request = new Request(LedgerRequestType.SEND_TRANSACTION, this.account, accountDestiny, 5,nonce.nextLong());
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

    public void getGlobalValue() {
        Request request = new Request(LedgerRequestType.GET_GLOBAL_VALUE);
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

    public void getTotalValue(List<byte[]> accounts) {
        Request request = new Request(LedgerRequestType.GET_TOTAL_VALUE,accounts);
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

    public void getExtract() {
        Request request = new Request(LedgerRequestType.GET_EXTRACT, this.account);
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

    public void loadMoney() {
        Request request = new Request(LedgerRequestType.LOAD_MONEY, this.account);
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

    public void getBalance() {
        Request request = new Request(LedgerRequestType.GET_LEDGER, this.account);
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

    public void createAccount() {
        Request request = new Request(LedgerRequestType.CREATE_ACCOUNT, this.account);
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
        } catch (ProcessingException pe) {
            pe.printStackTrace();
            throw new InternalServerErrorException();
        }

    }


    public Block getBlockToMine() {
        Request request = new Request(LedgerRequestType.GET_BLOCK_TO_MINE);
        request.setPublicKey(this.keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        WebTarget target = client.target( serverURI ).path("/mine/get");
        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKey(), request.getRequestType().toString().getBytes(), reply.getSignature()))
                    throw new InternalServerErrorException();
                return reply.getBlockReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch (ProcessingException pe) {
            pe.printStackTrace();
            throw new InternalServerErrorException();
        }
    }

    public byte[] getLastMinedBlock() {
        Request request = new Request(LedgerRequestType.GET_LAST_MINED_BLOCK);
        request.setPublicKey(this.keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        WebTarget target = client.target( serverURI ).path("/mine/last");
        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKey(), request.getRequestType().toString().getBytes(), reply.getSignature()))
                    throw new InternalServerErrorException();
                return reply.getByteReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch (ProcessingException pe) {
            pe.printStackTrace();
            throw new InternalServerErrorException();
        }
    }

    public void mineBlock(Block block) {
        Request request = new Request(LedgerRequestType.MINE_BLOCK, block);
        request.setPublicKey(this.keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        WebTarget target = client.target( serverURI ).path("/mine/add");
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
        } catch (ProcessingException pe) {
            pe.printStackTrace();
            throw new InternalServerErrorException();
        }
    }
}

