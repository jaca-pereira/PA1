package proxy;


import Security.Security;
import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.core.messages.TOMMessageType;
import data.ProxyReply;
import data.Request;
import data.LedgerRequestType;
import data.Reply;

import java.io.*;

import java.security.KeyPair;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


public class Service implements ServiceAPI {
    private AsynchServiceProxy asynchServiceProxy;
    private KeyPair keyPair;

    public Service(int clientId) {
        this.keyPair = Security.getKeyPair();
        this.asynchServiceProxy = new AsynchServiceProxy(clientId, "config");
    }

    private byte[] sendRequest (byte[] request, TOMMessageType type, LedgerRequestType requestType) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutput objOut = new ObjectOutputStream(byteOut);
            Request deserialized = Request.deserialize(request);

            if (!Security.verifySignature(deserialized.getPublicKey(), deserialized.getRequestType().toString().getBytes(), deserialized.getSignature()))
                throw new IllegalArgumentException("Signature not valid!");

            objOut.flush();
            objOut.writeObject(deserialized);
            byteOut.flush();
            BlockingQueue<List<Reply>> replyChain = new LinkedBlockingDeque<>();
            ReplyListener replyListener = new ReplyListenerImp(replyChain, this.asynchServiceProxy);
            this.asynchServiceProxy.invokeAsynchRequest(byteOut.toByteArray(), replyListener, type);

            List<Reply> replicaReplies = replyChain.take();
            ProxyReply proxyReply = new ProxyReply();
            replicaReplies.forEach(rep -> {
                rep.setPublicKey(this.keyPair.getPublic().getEncoded());
                rep.setSignature(Security.signRequest(this.keyPair.getPrivate(), requestType.toString().getBytes()));
                proxyReply.addReply(rep);
            });
            return ProxyReply.serialize(proxyReply);

        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] createAccount(byte[] request) {
        return this.sendRequest(request, TOMMessageType.ORDERED_REQUEST, LedgerRequestType.CREATE_ACCOUNT);
    }

    @Override
    public byte[] loadMoney(byte[] request) {
        return this.sendRequest(request, TOMMessageType.ORDERED_REQUEST, LedgerRequestType.LOAD_MONEY);
    }

    @Override
    public byte[] getBalance(byte[] request) {
        return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST, LedgerRequestType.GET_BALANCE);
    }

    @Override
    public byte[] getExtract(byte[] request) {
        return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST, LedgerRequestType.GET_EXTRACT);
    }

    @Override
    public byte[] sendTransaction(byte[] request) {
        return this.sendRequest(request, TOMMessageType.ORDERED_REQUEST, LedgerRequestType.SEND_TRANSACTION);
    }

    @Override
    public byte[] getTotalValue(byte[] request) {
        return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST, LedgerRequestType.GET_TOTAL_VALUE);
    }

    @Override
    public byte[] getGlobalValue(byte[] request) {
        return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST, LedgerRequestType.GET_GLOBAL_VALUE);
    }

    @Override
    public byte[] getLedger(byte[] request) {
        return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST, LedgerRequestType.GET_LEDGER);
    }
}
