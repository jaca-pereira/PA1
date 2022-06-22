package data;

import java.io.IOException;
import java.net.*;

import java.nio.ByteBuffer;
import java.security.*;

import java.security.cert.CertificateException;
import java.util.*;


import Security.Security;


import org.glassfish.jersey.client.ClientConfig;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import Security.InsecureHostNameVerifier;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class Client {

    public static final int DEFAULT_AMOUNT_TRANSACTIONS = 5;
    private final URI proxyURI;
    private final javax.ws.rs.client.Client client;

    private Map<String, byte[]> accounts;

    public Client(URI proxyURI) {
        this.proxyURI = proxyURI;
        this.client = this.startClient();
        this.accounts = new HashMap<>();
    }

    private byte[] idMaker(String email) throws NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, IOException, KeyStoreException {
        SecureRandom generator = new SecureRandom();
        byte[] srn = generator.generateSeed(32);

        byte[] timer = String.valueOf(System.currentTimeMillis()).getBytes();
        byte[] emailBytes = email.getBytes();
        ByteBuffer buffersha256 = ByteBuffer.wrap(new byte[emailBytes.length + srn.length + timer.length]);
        buffersha256.put(emailBytes);
        buffersha256.put(srn);
        buffersha256.put(timer);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] sha256 = digest.digest(buffersha256.array());
        byte[] publicKey = Security.getKeyPair(email).getPublic().getEncoded();
        ByteBuffer buffer = ByteBuffer.wrap(new byte[sha256.length + publicKey.length]);
        buffer.put(sha256);
        buffer.put(publicKey);

        return buffer.array();
    }

    private javax.ws.rs.client.Client startClient() {

        try {
            SSLContext context = Security.getContext();
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


    public void getLedger(String account_alias) {
        try {
            Request request = new Request(LedgerRequestType.GET_LEDGER);
            KeyPair keyPair = Security.getKeyPair(account_alias);
            request.setPublicKey(keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("ledger");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (reply==null)
                    throw new WebApplicationException("Bizantine error!");
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy()))
                    throw new WebApplicationException("Bizantine error!");
                LedgerDataStructure ledger = reply.getLedgerReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch (ProcessingException | IOException |
                 NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new InternalServerErrorException();
        } catch (KeyStoreException | CertificateException e) {
            throw new WebApplicationException("Account not created!", Response.Status.NOT_FOUND);
        }

    }

    public void sendTransaction(String originAccount, String destinationAccount) {
        try {
            SecureRandom nonce = new SecureRandom();
            byte[] originAccountBytes = this.accounts.get(originAccount);
            byte[] destinationAccountBytes = this.accounts.get(destinationAccount);
            Request request = new Request(LedgerRequestType.SEND_TRANSACTION, originAccountBytes, destinationAccountBytes, DEFAULT_AMOUNT_TRANSACTIONS,nonce.nextLong());
            KeyPair keyPair = Security.getKeyPair(originAccount);
            request.setPublicKey(keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("transaction");

            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if(  r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (reply==null)
                    throw new WebApplicationException("Bizantine error!");
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy()))
                    throw new WebApplicationException("Bizantine error!");
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException | UnrecoverableKeyException | NoSuchAlgorithmException | IOException e) {
            throw new InternalServerErrorException();
        } catch (CertificateException | KeyStoreException e) {
            throw new WebApplicationException("Account not created!", Response.Status.NOT_FOUND);
        }
    }

    public void getGlobalValue(String account) {
        try {
            Request request = new Request(LedgerRequestType.GET_GLOBAL_VALUE);
            KeyPair keyPair = Security.getKeyPair(account);
            request.setPublicKey(keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("value");

            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (reply==null)
                    throw new WebApplicationException("Bizantine error!");
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy()))
                    throw new WebApplicationException("Bizantine error!");
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException | UnrecoverableKeyException | NoSuchAlgorithmException | IOException e) {
            throw new InternalServerErrorException();
        } catch (CertificateException | KeyStoreException e) {
            throw new WebApplicationException("Account not created!", Response.Status.NOT_FOUND);
        }
    }

    public void getTotalValue(String account, List<String> accounts) {
        try {
            List<byte[]> accountsBytes = new ArrayList<>(accounts.size());
            for (String acc: accounts) {
                accountsBytes.add( this.accounts.get(acc));
            }
            Request request = new Request(LedgerRequestType.GET_TOTAL_VALUE,accountsBytes);
            KeyPair keyPair = Security.getKeyPair(account);
            request.setPublicKey(keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("accounts/value");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (reply==null)
                    throw new WebApplicationException("Bizantine error!");
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy()))
                    throw new WebApplicationException("Bizantine error!");
                int value = reply.getIntReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException | UnrecoverableKeyException | NoSuchAlgorithmException | IOException e) {
            throw new InternalServerErrorException();
        } catch (CertificateException | KeyStoreException e) {
            throw new WebApplicationException("Account not created!", Response.Status.NOT_FOUND);
        }
    }

    public void getExtract(String account) {
        try {
            byte[] accountBytes = this.accounts.get(account);
            Request request = new Request(LedgerRequestType.GET_EXTRACT, accountBytes);
            KeyPair keyPair = Security.getKeyPair(account);
            request.setPublicKey(keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("account/extract");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (reply==null)
                    throw new WebApplicationException("Bizantine error!");
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy()))
                    throw new WebApplicationException("Bizantine error!");
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException | UnrecoverableKeyException | NoSuchAlgorithmException | IOException e) {
            throw new InternalServerErrorException();
        } catch (CertificateException | KeyStoreException e) {
            throw new WebApplicationException("Account not created!", Response.Status.NOT_FOUND);
        }
    }

    public void getBalance(String account) {
        try {
            byte[] accountBytes = this.accounts.get(account);
            Request request = new Request(LedgerRequestType.GET_BALANCE, accountBytes);
            KeyPair keyPair = Security.getKeyPair(account);
            request.setPublicKey(keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("account/balance");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (reply==null)
                    throw new WebApplicationException("Bizantine error!");
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy()))
                    throw new WebApplicationException("Bizantine error!");
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException | UnrecoverableKeyException | NoSuchAlgorithmException | IOException e) {
            throw new InternalServerErrorException();
        } catch (CertificateException | KeyStoreException e) {
            throw new WebApplicationException("Account not created!", Response.Status.NOT_FOUND);
        }
    }

    public void createAccount(String email) {
        try {
            byte[] account = this.idMaker(email);
            Request request = new Request(LedgerRequestType.CREATE_ACCOUNT, account);
            KeyPair keyPair = Security.getKeyPair(email);
            request.setPublicKey(keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("account");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (reply==null)
                    throw new WebApplicationException("Bizantine error!");
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy()))
                    throw new WebApplicationException("Bizantine error!");
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException | UnrecoverableKeyException | NoSuchAlgorithmException | IOException e) {
            throw new InternalServerErrorException();
        } catch (CertificateException | KeyStoreException e) {
            throw new WebApplicationException("Account not created!", Response.Status.NOT_FOUND);
        }

    }


    public Block getBlockToMine(String account) {
        try {
            Request request = new Request(LedgerRequestType.GET_BLOCK_TO_MINE);
            KeyPair keyPair = Security.getKeyPair(account);
            request.setPublicKey(keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("/mine/get");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (reply==null)
                    throw new WebApplicationException("Bizantine error!");
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy()))
                    throw new WebApplicationException("Bizantine error!");
                return reply.getBlockReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException | UnrecoverableKeyException | NoSuchAlgorithmException | IOException e) {
            throw new InternalServerErrorException();
        } catch (CertificateException | KeyStoreException e) {
            throw new WebApplicationException("Account not created!", Response.Status.NOT_FOUND);
        }
    }

    public void mineBlock(String account, Block block) {
        try {
            byte[] accountBytes = this.accounts.get(account);
            Request request = new Request(LedgerRequestType.MINE_BLOCK, accountBytes, block);
            KeyPair keyPair = Security.getKeyPair(account);
            request.setPublicKey(keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("/mine/add");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (reply==null)
                    throw new WebApplicationException("Bizantine error!");
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy()))
                    throw new WebApplicationException("Bizantine error!");
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException | UnrecoverableKeyException | NoSuchAlgorithmException | IOException e) {
            throw new InternalServerErrorException();
        } catch (CertificateException | KeyStoreException e) {
            throw new WebApplicationException("Account not created!", Response.Status.NOT_FOUND);
        }
    }
}

