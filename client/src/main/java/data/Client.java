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

    private final String proxyURI;
    private final javax.ws.rs.client.Client client;

    private Map<String, byte[]> accounts;
    private KeyPair keyPair;
    private final boolean sgx;
    private final String sgxURI;

    public Client(String proxyURI, boolean sgx) throws NoSuchAlgorithmException {
        this.proxyURI = proxyURI;
        this.client = this.startClient();
        this.accounts = new HashMap<>();
        this.keyPair = Security.getKeyPair();
        this.sgx = sgx;
        this.sgxURI = null;
    }

    public Client(String proxyURI, boolean sgx, String sgxURI) throws NoSuchAlgorithmException {
        this.proxyURI = proxyURI;
        this.client = this.startClient();
        this.accounts = new HashMap<>();
        this.keyPair = Security.getKeyPair();
        this.sgx = sgx;
        this.sgxURI = sgxURI;
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

    private byte[] getUser(String account) {
        return accounts.get(account);
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
                    System.out.println("OPERAÇÃO NÃO FOI POSSÍVEL");
                    return new LinkedList<>();
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("ERRO");
                    System.out.println(reply.getError().getMessage());
                    return new LinkedList<>();
                }
                return (List<Block>) reply.getListReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch (ProcessingException e) {
            throw new InternalServerErrorException();
        }
    }

    public boolean sendTransaction(String originAccount, String destinationAccount, int value) {
        try {
            SecureRandom nonce = new SecureRandom();
            byte[] originAccountBytes = this.getUser(originAccount);
            byte[] destinationAccountBytes;
            do {
                destinationAccountBytes = this.getUser(destinationAccount);
            } while (new String (originAccountBytes).equals(new String(destinationAccountBytes)));
            Request request = new Request(LedgerRequestType.SEND_TRANSACTION, originAccountBytes, destinationAccountBytes, value,nonce.nextLong());
            request.setPublicKey(this.keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target;
            if (!sgx)
                target = this.client.target(proxyURI).path("transaction");
            else target = this.client.target(sgxURI).path("transaction");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if(  r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                List<byte[]> replies = proxyReply.getReplicaReplies();
                if (replies.size()==0) {
                    System.out.println("OPERAÇÃO NÃO FOI POSSÍVEL");
                    return false;
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("ERRO");
                    System.out.println(reply.getError().getMessage());
                    return false;
                }
                return true;
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
                    System.out.println("OPERAÇÃO NÃO FOI POSSÍVEL");
                    return -1;
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("ERRO");
                    System.out.println(reply.getError().getMessage());
                    return -1;
                }
                return reply.getIntReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException e) {
            throw new InternalServerErrorException();
        }
    }

    public int getTotalValue(List<String> accounts) {
        try {
            List<byte[]> accountsBytes = new LinkedList();
            for (String account: accounts)
                accountsBytes.add(this.getUser(account));

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
                    System.out.println("OPERAÇÃO NÃO FOI POSSÍVEL");
                    return -1;
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("ERRO");
                    System.out.println(reply.getError().getMessage());
                    return -1;
                }
                return reply.getIntReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException e) {
            throw new InternalServerErrorException();
        }
    }

    public List<Transaction> getExtract(String account) {
        try {
            byte[] accountBytes = this.getUser(account);
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
                    System.out.println("OPERAÇÃO NÃO FOI POSSÍVEL");
                    return new LinkedList<>();
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("ERRO");
                    System.out.println(reply.getError().getMessage());
                    return new LinkedList<>();
                }
                return (List<Transaction>) reply.getListReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException e) {
            throw new InternalServerErrorException();
        }
    }

    public int getBalance(String account) {
        try {
            byte[] accountBytes = this.getUser(account);
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
                    System.out.println("OPERAÇÃO NÃO FOI POSSÍVEL");
                    return -1;
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("ERRO");
                    System.out.println(reply.getError().getMessage());
                    return -1;
                }
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
                    System.out.println("OPERAÇÃO NÃO FOI POSSÍVEL");
                    return;
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("ERRO");
                    System.out.println(reply.getError().getMessage());
                    return;
                }
                accounts.put(email, account);
            } else {
                throw new WebApplicationException(r.getStatus());
            }
        } catch ( ProcessingException e) {
            e.printStackTrace();
            throw new WebApplicationException();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }


    public Block getBlockToMine(String account) {
        try {
            Request request = new Request(LedgerRequestType.GET_BLOCK_TO_MINE);
            request.setPublicKey(keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("/mine/get");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                List<byte[]> replies = proxyReply.getReplicaReplies();
                if (replies.size()==0) {
                    System.out.println("OPERAÇÃO NÃO FOI POSSÍVEL");
                    return null;
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("ERRO");
                    System.out.println(reply.getError().getMessage());
                    return null;
                }
                Block block = reply.getBlockReply();
                block.setAccount(accounts.get(account));
                System.out.println("TEM UM BLOCO" + block.getLastBlockHash());
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
                    System.out.println("OPERAÇÃO NÃO FOI POSSÍVEL");
                    return;
                }
                Reply reply = Reply.deserialize(replies.get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("ERRO");
                    System.out.println(reply.getError().getMessage());
                    return;
                }
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException e) {
            throw new InternalServerErrorException();
        }
    }


}

