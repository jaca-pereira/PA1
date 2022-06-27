package data;


import java.net.*;

import java.nio.ByteBuffer;
import java.security.*;

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

    private final URI proxyURI;
    private final javax.ws.rs.client.Client client;

    private Map<Integer, byte[]> accounts;
    private KeyPair keyPair;
    private int currentUser;

    public Client(URI proxyURI) throws NoSuchAlgorithmException {
        this.proxyURI = proxyURI;
        this.client = this.startClient();
        this.accounts = new HashMap<>();
        this.keyPair = Security.getKeyPair();
        this.currentUser = -1;
    }

    private byte[] idMaker(String email) throws NoSuchAlgorithmException {
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
        byte[] publicKey = this.keyPair.getPublic().getEncoded();
        ByteBuffer buffer = ByteBuffer.wrap(new byte[sha256.length + publicKey.length]);
        buffer.put(sha256);
        buffer.put(publicKey);

        return buffer.array();
    }

    private byte[] getCurrentUser() {
        currentUser++;
        if (currentUser >= accounts.size())
            currentUser = 0;
        return accounts.get(currentUser);
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


    public List<Block> getLedger() {
        try {
            Request request = new Request(LedgerRequestType.GET_LEDGER);
            request.setPublicKey(this.keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("ledger");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                List<byte[]> replies = proxyReply.getReplicaReplies();
                if (replies.size()==0) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                if (reply.getError()!=null)
                    throw new WebApplicationException(reply.getError());
                return (List<Block>) reply.getListReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch (ProcessingException e) {
            throw new InternalServerErrorException();
        }
    }

    public void sendTransaction(int value) {
        try {
            SecureRandom nonce = new SecureRandom();
            byte[] originAccountBytes = this.getCurrentUser();
            byte[] destinationAccountBytes;
            do {
                destinationAccountBytes = this.getCurrentUser();
            } while (new String (originAccountBytes).equals(new String(destinationAccountBytes)));
            Request request = new Request(LedgerRequestType.SEND_TRANSACTION, originAccountBytes, destinationAccountBytes, value,nonce.nextLong());
            request.setPublicKey(this.keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("transaction");

            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if(  r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                List<byte[]> replies = proxyReply.getReplicaReplies();
                if (replies.size()==0) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                if (reply.getError()!=null)
                    throw new WebApplicationException(reply.getError());
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException e) {
            throw new InternalServerErrorException();
        }
    }

    public int getGlobalValue() {
        try {
            Request request = new Request(LedgerRequestType.GET_GLOBAL_VALUE);
            request.setPublicKey(this.keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("value");

            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                List<byte[]> replies = proxyReply.getReplicaReplies();
                if (replies.size()==0) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                if (reply.getError()!=null)
                    throw new WebApplicationException(reply.getError());
                return reply.getIntReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException e) {
            throw new InternalServerErrorException();
        }
    }

    public int getTotalValue() {
        try {
            List<byte[]> accountsBytes = new ArrayList<>(accounts.size());
            for (int i = 0; i < 3; i++)
                accountsBytes.add(this.getCurrentUser());

            Request request = new Request(LedgerRequestType.GET_TOTAL_VALUE,accountsBytes);
            request.setPublicKey(this.keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("accounts/value");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                List<byte[]> replies = proxyReply.getReplicaReplies();
                if (replies.size()==0) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                if (reply.getError()!=null)
                    throw new WebApplicationException(reply.getError());
                return reply.getIntReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException e) {
            throw new InternalServerErrorException();
        }
    }

    public List<Transaction> getExtract() {
        try {
            byte[] accountBytes = this.getCurrentUser();
            Request request = new Request(LedgerRequestType.GET_EXTRACT, accountBytes);
            request.setPublicKey(this.keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("account/extract");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                List<byte[]> replies = proxyReply.getReplicaReplies();
                if (replies.size()==0) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                if (reply.getError()!=null)
                    throw new WebApplicationException(reply.getError());
                return (List<Transaction>) reply.getListReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException e) {
            throw new InternalServerErrorException();
        }
    }

    public int getBalance() {
        try {
            byte[] accountBytes = this.getCurrentUser();
            Request request = new Request(LedgerRequestType.GET_BALANCE, accountBytes);
            request.setPublicKey(this.keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("account/balance");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                List<byte[]> replies = proxyReply.getReplicaReplies();
                if (replies.size()==0) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                if (reply.getError()!=null)
                    throw new WebApplicationException(reply.getError());
                return reply.getIntReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException e) {
            throw new InternalServerErrorException();
        }
    }

    public void createAccount(String email) {
        try {
            byte[] account = this.idMaker(email);
            Request request = new Request(LedgerRequestType.CREATE_ACCOUNT, account);
            request.setPublicKey(this.keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = client.target(proxyURI).path("account");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                List<byte[]> replies = proxyReply.getReplicaReplies();
                if (replies.size()==0) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                if (reply.getError()!=null)
                    throw new WebApplicationException(reply.getError());
                accounts.put(accounts.size(), account);
            } else {
                throw new WebApplicationException(r.getStatus());
            }
        } catch ( ProcessingException e) {
            throw new WebApplicationException();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }


    public Block getBlockToMine() {
        try {
            Request request = new Request(LedgerRequestType.GET_BLOCK_TO_MINE);
            request.setPublicKey(keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            request.setPublicKey(this.keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("/mine/get");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                List<byte[]> replies = proxyReply.getReplicaReplies();
                if (replies.size()==0) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                if (reply.getError()!=null) {
                    System.out.println("TEM ERRO");
                    throw new WebApplicationException(reply.getError());
                }
                System.out.println("RESPONDEU O BLOCO");
                Block block = reply.getBlockReply();
                block.setAccount(this.getCurrentUser());

                return block;
            } else {
                System.out.println("ERRO DE RESPOSTA " );
                throw new WebApplicationException(r.getStatus());

            }
        } catch ( ProcessingException e){

        }
        return null;
    }

    public void mineBlock(Block block) {
        try {
            byte[] accountBytes = block.getAccount();
            block.setSignature((Security.signRequest(this.keyPair.getPrivate(), LedgerRequestType.MINE_BLOCK.toString().getBytes())));
            block.setPublicKey(this.keyPair.getPublic().getEncoded());
            Request request = new Request(LedgerRequestType.MINE_BLOCK, accountBytes, block);
            request.setPublicKey(this.keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("/mine/add");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                List<byte[]> replies = proxyReply.getReplicaReplies();
                if (replies.size()==0) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                if (reply.getError()!=null)
                    throw new WebApplicationException(reply.getError());
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException e) {
            throw new InternalServerErrorException();
        }
    }


}

