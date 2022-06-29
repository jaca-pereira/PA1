package proxy;


import Security.Security;
import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;

import bftsmart.tom.core.messages.TOMMessageType;
import data.ProxyReply;
import data.Request;
import data.Reply;

import javax.ws.rs.InternalServerErrorException;
import java.io.*;

import java.security.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


public class Service implements ServiceAPI {
    private AsynchServiceProxy asyncServiceProxy;
    private KeyPair keyPair;

    public Service(int proxyId) {
        try {
            this.keyPair = Security.getKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerErrorException("Proxy key not created!");
        }
        this.asyncServiceProxy = new AsynchServiceProxy(proxyId);
    }
   private byte[] sendRequest(byte[] request, TOMMessageType type) {
       try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

           Request deserialized = Request.deserialize(request);
           System.out.println(deserialized.getRequestType());
           List<Reply> replicaReplies = new LinkedList<>();
           if (!Security.verifySignature(deserialized.getPublicKey(), deserialized.getRequestType().toString().getBytes(), deserialized.getSignature())) {
               Reply reply = new Reply("Request not properly signed!");
               replicaReplies.add(reply);
           } else {
               objOut.writeObject(deserialized);
               objOut.flush();
               byteOut.flush();
               BlockingQueue<List<Reply>> replyChain = new LinkedBlockingDeque<>();
               ReplyListener replyListener = new ReplyListenerImp(replyChain, this.asyncServiceProxy);
               this.asyncServiceProxy.invokeAsynchRequest(byteOut.toByteArray(), replyListener, type);
               replicaReplies = replyChain.take();
               if (replicaReplies.isEmpty())
                   replicaReplies.add(new Reply("Replicas não assinaram bem."));
           }
           ProxyReply proxyReply = new ProxyReply();
           replicaReplies.forEach(rep -> {
               rep.setPublicKeyProxy(this.keyPair.getPublic().getEncoded());
               rep.setSignatureProxy(Security.signRequest(this.keyPair.getPrivate(), deserialized.getRequestType().toString().getBytes()));
               proxyReply.addReply(rep);
           });
           return ProxyReply.serialize(proxyReply);
       } catch (InterruptedException | IOException e) {
           e.printStackTrace();
           return null;
       }
   }


    @Override
    public byte[] createAccount(byte[] request) {
        return this.sendRequest(request,TOMMessageType.ORDERED_REQUEST);
    }

    @Override
    public byte[] getBalance(byte[] request) {
        return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST);
    }

    @Override
    public byte[] getExtract(byte[] request) {
        return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST);
    }

    @Override
    public byte[] sendTransaction(byte[] request) {
        return this.sendRequest(request, TOMMessageType.ORDERED_REQUEST);
    }

    @Override
    public byte[] getTotalValue(byte[] request) {
        return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST);
    }

    @Override
    public byte[] getGlobalValue(byte[] request) {
       return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST);
    }

    @Override
    public byte[] getLedger(byte[] request) {
        return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST);
    }

    @Override
    public byte[] getBlockToMine(byte[] request) {
       return this.sendRequest(request, TOMMessageType.UNORDERED_REQUEST);
    }

    @Override
    public byte[] mineBlock(byte[] request) {
        return this.sendRequest(request, TOMMessageType.ORDERED_REQUEST);
    }

}
