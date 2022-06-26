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
    private KeyPair keyPair;
    private int currentUser;

    public Client(URI proxyURI) throws NoSuchAlgorithmException {
        this.proxyURI = proxyURI;
        this.client = this.startClient();
        this.accounts = new HashMap<>();
        this.keyPair = getKeyPair();
        this.currentUser = -1;
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


    public LedgerDataStructure getLedger(String account_alias) {
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
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    throw new WebApplicationException("Bizantine error!");
                }
                if (reply.getError()!=null)
                    throw new WebApplicationException(reply.getError());
                return reply.getLedgerReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch (ProcessingException e) {
            throw new InternalServerErrorException();
        }
    }

    public void sendTransaction(String originAccount, String destinationAccount) {
        try {
            SecureRandom nonce = new SecureRandom();
            byte[] originAccountBytes = this.accounts.get(originAccount);
            byte[] destinationAccountBytes = this.accounts.get(destinationAccount);
            Request request = new Request(LedgerRequestType.SEND_TRANSACTION, originAccountBytes, destinationAccountBytes, DEFAULT_AMOUNT_TRANSACTIONS,nonce.nextLong());
            request.setPublicKey(this.keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("transaction");

            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if(  r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
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

    public int getGlobalValue(String account) {
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
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
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

    public int getTotalValue(String account, List<String> accounts) {
        try {
            List<byte[]> accountsBytes = new ArrayList<>(accounts.size());
            for (String acc: accounts) {
                accountsBytes.add( this.accounts.get(acc));
            }
            Request request = new Request(LedgerRequestType.GET_TOTAL_VALUE,accountsBytes);
            request.setPublicKey(this.keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("accounts/value");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
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

    public List<Transaction> getExtract(String account) {
        try {
            byte[] accountBytes = this.accounts.get(account);
            Request request = new Request(LedgerRequestType.GET_EXTRACT, accountBytes);
            request.setPublicKey(this.keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("account/extract");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    throw new WebApplicationException("Bizantine error!");
                }
                if (reply.getError()!=null)
                    throw new WebApplicationException(reply.getError());
                return reply.getListReply();
            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException e) {
            throw new InternalServerErrorException();
        }
    }

    public int getBalance(String account) {
        try {
            byte[] accountBytes = this.accounts.get(account);
            Request request = new Request(LedgerRequestType.GET_BALANCE, accountBytes);
            request.setPublicKey(this.keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("account/balance");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
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
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    throw new WebApplicationException("Bizantine error!");
                }
                if (reply.getError()!=null)
                    throw new WebApplicationException(reply.getError());
                accounts.put(email, account);
            } else {
                throw new WebApplicationException(r.getStatus());
            }
        } catch ( ProcessingException | UnrecoverableKeyException | NoSuchAlgorithmException | IOException e) {
            throw new WebApplicationException();
        } catch(CertificateException | KeyStoreException e) {
            throw new WebApplicationException("Account keystore not created!");
        }

    }


    public Block getBlockToMine() {
        try {
            Request request = new Request(LedgerRequestType.GET_BLOCK_TO_MINE);
            KeyPair keyPair = Security.getKeyPair(account);
            System.out.println("TEM KEY PAIR");
            request.setPublicKey(keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
=======
            request.setPublicKey(this.keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
>>>>>>> Stashed changes
            WebTarget target = this.client.target(proxyURI).path("/mine/get");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
                    System.out.println("MAL ASSINADO RESPOSTA");
                    throw new WebApplicationException("Bizantine error!");
                }
                if (reply.getError()!=null) {
                    System.out.println("TEM ERRO");
                    throw new WebApplicationException(reply.getError());
                }
                System.out.println("RESPONDEU O BLOCO");
                return reply.getBlockReply();
            } else {
                System.out.println("ERRO DE RESPOSTA " );
                throw new WebApplicationException(r.getStatus());
<<<<<<< Updated upstream
            }
        } catch ( ProcessingException | UnrecoverableKeyException | NoSuchAlgorithmException | IOException e) {
            System.out.println("PROBLEMAS COM A CHAVE");
            throw new InternalServerErrorException();
        } catch (CertificateException | KeyStoreException e) {
            System.out.println("NAO HA CONTA");
            throw new WebApplicationException("Account not created!", Response.Status.NOT_FOUND);
=======
        } catch ( ProcessingException e) {
            throw new InternalServerErrorException();
>>>>>>> Stashed changes
        }
    }

    public void mineBlock(String account, Block block) {
        try {
            byte[] accountBytes = this.accounts.get(account);
            Request request = new Request(LedgerRequestType.MINE_BLOCK, accountBytes, block);
            request.setPublicKey(this.keyPair.getPublic().getEncoded());
            request.setSignature(Security.signRequest(this.keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
            WebTarget target = this.client.target(proxyURI).path("/mine/add");
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                ProxyReply proxyReply = ProxyReply.deserialize(r.readEntity(byte[].class));
                Reply reply = Reply.deserialize(proxyReply.getReplicaReplies().get(0));
                if (!Security.verifySignature(reply.getPublicKeyProxy(), reply.getRequestType().toString().getBytes(), reply.getSignatureProxy())) {
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

    private static KeyPair getKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        return keyPairGenerator.generateKeyPair();
    }
}

