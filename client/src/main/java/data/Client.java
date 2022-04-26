package data;

import java.io.FileInputStream;
import java.net.*;

import java.nio.ByteBuffer;
import java.security.*;

import java.util.List;


import Security.Security;

import data.Request;
import data.Transaction;
import org.glassfish.jersey.client.ClientConfig;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import Security.InsecureHostNameVerifier;
import data.Reply;

import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class Client {

    private URI serverURI;
    private javax.ws.rs.client.Client client;

    private KeyPair keyPair;
    public Client(URI proxyURI) {
        this.serverURI = proxyURI;
        this.client = startClient();
        this.keyPair = Security.getKeyPair();
    }
    private SSLContext getContext() throws Exception {
        KeyStore ts = KeyStore.getInstance("JKS");

        try (FileInputStream fis = new FileInputStream("security/clientcacerts.jks")) {
            ts.load(fis, "password".toCharArray());
        }


        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);

        String protocol = "TLSv1.2";
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
        return keyPair;
    }

    public String executeCommand(Request request) throws NoSuchAlgorithmException {
        switch (request.getRequestType()) {
            case CREATE_ACCOUNT:
                return createAccount(request) + "\n";
            case GET_BALANCE:
                return getBalance(request) + "\n";
            case LOAD_MONEY:
                return loadMoney(request) + "\n";
            case GET_EXTRACT:
                return getExtract(request) + "\n";
            case GET_TOTAL_VALUE:
                return getTotalValue(request) + "\n";
            case GET_GLOBAL_VALUE:
                return getGlobalValue(request) + "\n";
            case SEND_TRANSACTION:
                return sendTransaction(request) + "\n";
            case GET_LEDGER:
                return getLedger() + "\n";
            default:
                return "Command unknown!" + "\n";
        }
    }


    private String getLedger() {

        javax.ws.rs.client.Client client = startClient();
        WebTarget target = client.target( serverURI ).path("ledger");
        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(null);
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                List<Transaction> ledger = Reply.deserialize(r.readEntity(byte[].class)).getListReply();
                return "Success: " + ledger;
            } else
                return "Error, HTTP error status: " + r.getStatus();
        } catch ( ProcessingException pe ) {
            pe.printStackTrace();
            return "Timeout occurred.";
        }

    }

    private String sendTransaction(Request request) {

        request.setPublicKey(this.keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        javax.ws.rs.client.Client client = startClient();

        WebTarget target = client.target( serverURI ).path("transaction");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if(  r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                return  "Success: " + Reply.deserialize(r.readEntity(byte[].class)).getBoolReply();
            } else
                return "Error, HTTP error status: " + r.getStatus();

        } catch ( ProcessingException pe ) {
            pe.printStackTrace();
            return "Timeout occurred.";
        }
    }

    private String getGlobalValue(Request request) {
        request.setPublicKey(this.keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        WebTarget target = client.target( serverURI ).path("value");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                int value = Reply.deserialize(r.readEntity(byte[].class)).getIntReply();
                return "Success: " + value;
            } else
                return "Error, HTTP error status: " + r.getStatus();

        } catch ( ProcessingException pe ) {
            pe.printStackTrace(); //
            return "Timeout occurred.";
        }
    }

    private String getTotalValue(Request request) {
        request.setPublicKey(this.keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        WebTarget target = client.target( serverURI ).path("accounts/value");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                int value = Reply.deserialize(r.readEntity(byte[].class)).getIntReply();
                return "Success: " + value;
            } else
                return "Error, HTTP error status: " + r.getStatus();

        } catch ( ProcessingException pe ) {
            pe.printStackTrace(); //
            return "Timeout occurred.";
        }
    }

    private String getExtract(Request request) {
        request.setPublicKey(this.keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        WebTarget target = client.target( serverURI ).path("account/extract");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                List<Transaction> extract = Reply.deserialize(r.readEntity(byte[].class)).getListReply();
                return "Success: " + extract;
            } else
                return "Error, HTTP error status: " + r.getStatus();

        } catch ( ProcessingException pe ) {
            pe.printStackTrace();
            return "Timeout occurred.";
        }
    }

    private String loadMoney(Request request) {
        request.setPublicKey(this.keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        WebTarget target = client.target( serverURI ).path("account/load");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if(  r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                return "Success: " + Reply.deserialize(r.readEntity(byte[].class)).getBoolReply();
            } else
                return "Error, HTTP error status: " + r.getStatus();

        } catch ( ProcessingException pe ) {
            pe.printStackTrace();
            return "Timeout occurred.";
        }
    }

    private String getBalance(Request request) {

        request.setPublicKey(this.keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        WebTarget target = client.target( serverURI ).path("account/balance");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                int balance = Reply.deserialize(r.readEntity(byte[].class)).getIntReply();
                return "Success: " + balance;
            } else
                return "Error, HTTP error status: " + r.getStatus();

        } catch ( ProcessingException pe ) {
            pe.printStackTrace();
            return "Timeout occurred.";
        }
    }

    private String createAccount(Request request) throws NoSuchAlgorithmException {

        request.setPublicKey(this.keyPair.getPublic().getEncoded());

        request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        WebTarget target = client.target( serverURI ).path("account");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));

            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                byte[] acc = Reply.deserialize(r.readEntity(byte[].class)).getByteReply();
                return "Success: " + acc;
            } else
                return "Error, HTTP error status: " + r.getStatus();

        } catch ( ProcessingException pe ) { //Error in communication with server
            pe.printStackTrace();
            return "Timeout occurred.";
        }

    }

    private byte[] accountIdCreator(byte[] sha256) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[sha256.length + this.keyPair.getPublic().getEncoded().length]);
        buffer.put(sha256);
        buffer.put(this.keyPair.getPublic().getEncoded());

        return buffer.array();
    }


}
