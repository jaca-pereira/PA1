package proxy;


import Security.Security;
import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.core.messages.TOMMessageType;
import data.ProxyReply;
import data.Request;
import data.LedgerRequestType;
import data.Reply;

import javax.ws.rs.InternalServerErrorException;
import java.io.*;

import java.security.*;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


public class Service implements ServiceAPI {
    private final boolean asynch;
    private AsynchServiceProxy asynchServiceProxy;
    private ServiceProxy serviceProxy;
    private KeyPair keyPair;

    public Service(int proxyId, String alias, boolean asynch) {
        try {
            this.keyPair = Security.getKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerErrorException("Proxy key not created!");
        }
        this.asynch = asynch;
        if (this.asynch)
            this.asynchServiceProxy = new AsynchServiceProxy(proxyId);
        else this.serviceProxy = new ServiceProxy(proxyId);

    }

    private byte[] sendRequest (byte[] request, TOMMessageType type) {

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            Request deserialized = Request.deserialize(request);
            if (!Security.verifySignature(deserialized.getPublicKey(), deserialized.getRequestType().toString().getBytes(), deserialized.getSignature())) {
                return Reply.serialize(new Reply("Request not properly signed!"));
            }
            objOut.writeObject(deserialized);
            objOut.flush();
            byteOut.flush();
            byte[] reply = serviceProxy.invoke(byteOut.toByteArray(), type);
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Reply rep = (Reply) objIn.readObject();
                return Reply.serialize(rep);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
            return Reply.serialize(new Reply(e.getMessage()));
        }


    }



    private byte[] sendRequestAsynch (byte[] request, TOMMessageType type, LedgerRequestType requestType) {

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            Request deserialized = Request.deserialize(request);
            if (!Security.verifySignature(deserialized.getPublicKey(), deserialized.getRequestType().toString().getBytes(), deserialized.getSignature())) {
                return Reply.serialize(new Reply("Request not properly signed!"));
            }
            objOut.writeObject(deserialized);
            objOut.flush();
            byteOut.flush();
            BlockingQueue<List<Reply>> replyChain = new LinkedBlockingDeque<>();
            ReplyListener replyListener = new ReplyListenerImp(replyChain, this.asynchServiceProxy);
            this.asynchServiceProxy.invokeAsynchRequest(request, replyListener, type);
            List<Reply> replicaReplies = replyChain.take();
            ProxyReply proxyReply = new ProxyReply();
            replicaReplies.forEach(rep -> {
                rep.setPublicKeyProxy(this.keyPair.getPublic().getEncoded());
                rep.setSignatureProxy(Security.signRequest(this.keyPair.getPrivate(), requestType.toString().getBytes()));
                proxyReply.addReply(rep);
            });
            return ProxyReply.serialize(proxyReply);
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            return Reply.serialize(new Reply(e.getMessage()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] createAccount(byte[] request) {
        if (this.asynch)
            return this.sendRequestAsynch(request, TOMMessageType.ORDERED_REQUEST, LedgerRequestType.CREATE_ACCOUNT);
        else return this.sendRequest(request,TOMMessageType.ORDERED_REQUEST);
    }

    @Override
    public byte[] getBalance(byte[] request) {
        if (this.asynch)
            return this.sendRequestAsynch(request, TOMMessageType.UNORDERED_REQUEST, LedgerRequestType.GET_BALANCE);
        else return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST);
    }

    @Override
    public byte[] getExtract(byte[] request) {
        if (this.asynch)
            return this.sendRequestAsynch(request, TOMMessageType.UNORDERED_REQUEST, LedgerRequestType.GET_EXTRACT);
        else return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST);
    }

    @Override
    public byte[] sendTransaction(byte[] request) {
        if (this.asynch)
            return this.sendRequestAsynch(request, TOMMessageType.ORDERED_REQUEST, LedgerRequestType.SEND_TRANSACTION);
        else return this.sendRequest(request, TOMMessageType.ORDERED_REQUEST);
    }

    @Override
    public byte[] getTotalValue(byte[] request) {
        if (this.asynch)
            return this.sendRequestAsynch(request, TOMMessageType.UNORDERED_REQUEST, LedgerRequestType.GET_TOTAL_VALUE);
        else return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST);
    }

    @Override
    public byte[] getGlobalValue(byte[] request) {
       if (this.asynch)
            return this.sendRequestAsynch(request, TOMMessageType.UNORDERED_REQUEST, LedgerRequestType.GET_GLOBAL_VALUE);
        else return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST);
    }

    @Override
    public byte[] getLedger(byte[] request) {
        if (this.asynch)
            return this.sendRequestAsynch(request, TOMMessageType.UNORDERED_REQUEST, LedgerRequestType.GET_LEDGER);
        else return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST);
    }

    @Override
    public byte[] getBlockToMine(byte[] request) {
       if (this.asynch)
            return this.sendRequestAsynch(request, TOMMessageType.UNORDERED_REQUEST, LedgerRequestType.GET_BLOCK_TO_MINE);
        else return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST);
    }

    @Override
    public byte[] mineBlock(byte[] request) {
        if (this.asynch)
            return this.sendRequestAsynch(request, TOMMessageType.ORDERED_REQUEST, LedgerRequestType.MINE_BLOCK);
        else return this.sendRequest(request, TOMMessageType.ORDERED_REQUEST);
    }

}
