package proxy;


import Security.Security;
import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.core.messages.TOMMessageType;
import data.Request;
import data.LedgerRequestType;
import data.Reply;

import java.io.*;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


public class Service implements ServiceAPI {
    private AsynchServiceProxy asynchServiceProxy;
    private KeyPair keyPair;

    public Service(int clientId) {
        this.keyPair = Security.getKeyPair();
        this.asynchServiceProxy = new AsynchServiceProxy(clientId);
    }

    private BlockingQueue<List<Reply>> sendRequest (byte[] request, TOMMessageType type, BlockingQueue<List<Reply>> replyChain) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutput objOut = new ObjectOutputStream(byteOut);
            Request deserialized = Request.deserialize(request);

            if (!Security.verifySignature(deserialized.getPublicKey(), deserialized.getRequestType().toString().getBytes(), deserialized.getSignature()))
                throw new IllegalArgumentException("Signature not valid!");

            objOut.writeObject(Request.deserialize(request));
            objOut.flush();
            byteOut.flush();
            ReplyListener replyListener = new ReplyListenerImp(replyChain, this.asynchServiceProxy);
            this.asynchServiceProxy.invokeAsynchRequest(byteOut.toByteArray(), replyListener, type);

            return replyChain;

        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            return null;
        }
    }

    private byte[] getReply(BlockingQueue<List<Reply>> replyChain, LedgerRequestType type) {
        try {
            List<Reply> replicaReplies = replyChain.take();
            List<byte[]> finalReply = new ArrayList<>(replicaReplies.size());
            replicaReplies.forEach(rep -> {
                rep.setPublicKey(this.keyPair.getPublic().getEncoded());
                rep.setSignature(Security.signRequest(this.keyPair.getPrivate(), type.toString().getBytes()));
                finalReply.add(Reply.serialize(rep));
            });
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(finalReply);
            out.flush();
            os.flush();
            return out.toByteArray();
        } catch (IOException ex) {
            System.out.println("Exception: " + ex.getMessage());
        } catch (InterruptedException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
        return null;

    }
    @Override
    public byte[] createAccount(byte[] request) {
        BlockingQueue<List<Reply>> replyChain = new LinkedBlockingDeque<>();
        this.sendRequest(request, TOMMessageType.ORDERED_REQUEST, replyChain);
        return this.getReply(replyChain, LedgerRequestType.CREATE_ACCOUNT);
    }

    @Override
    public byte[] loadMoney(byte[] request) {
        BlockingQueue<List<Reply>> replyChain = new LinkedBlockingDeque<>();
        this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST, replyChain);
        return this.getReply(replyChain, LedgerRequestType.LOAD_MONEY);
    }

    @Override
    public byte[] getBalance(byte[] request) {
        BlockingQueue<List<Reply>> replyChain = new LinkedBlockingDeque<>();
        this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST, replyChain);
        return this.getReply(replyChain, LedgerRequestType.GET_BALANCE);
    }

    @Override
    public byte[] getExtract(byte[] request) {
        BlockingQueue<List<Reply>> replyChain = new LinkedBlockingDeque<>();
        this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST, replyChain);
        return this.getReply(replyChain, LedgerRequestType.GET_EXTRACT);
    }

    @Override
    public byte[] sendTransaction(byte[] request) {
        BlockingQueue<List<Reply>> replyChain = new LinkedBlockingDeque<>();
        this.sendRequest(request, TOMMessageType.ORDERED_REQUEST, replyChain);
        return this.getReply(replyChain, LedgerRequestType.SEND_TRANSACTION);
    }

    @Override
    public byte[] getTotalValue(byte[] request) {
        BlockingQueue<List<Reply>> replyChain = new LinkedBlockingDeque<>();
        this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST, replyChain);
        return this.getReply(replyChain, LedgerRequestType.GET_TOTAL_VALUE);
    }

    @Override
    public byte[] getGlobalValue(byte[] request) {
        BlockingQueue<List<Reply>> replyChain = new LinkedBlockingDeque<>();
        this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST, replyChain);
        return this.getReply(replyChain, LedgerRequestType.GET_GLOBAL_VALUE);
    }

        @Override
    public byte[] getLedger() {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
            objOut.writeObject(new Request(LedgerRequestType.GET_LEDGER));
            objOut.flush();
            byteOut.flush();
            BlockingQueue<List<Reply>> replyChain = new LinkedBlockingDeque<>();
            this.sendRequest(byteOut.toByteArray(), TOMMessageType.UNORDERED_REQUEST, replyChain);
            return this.getReply(replyChain, LedgerRequestType.GET_LEDGER);

        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }
}
