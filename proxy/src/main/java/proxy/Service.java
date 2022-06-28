package proxy;


import Security.Security;
import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.ServiceProxy;
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
    private final boolean async;
    private AsynchServiceProxy asyncServiceProxy;
    private ServiceProxy serviceProxy;
    private KeyPair keyPair;

    public Service(int proxyId, boolean asynch) {
        try {
            this.keyPair = Security.getKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerErrorException("Proxy key not created!");
        }
        this.async = asynch;
        if (this.async)
            this.asyncServiceProxy = new AsynchServiceProxy(proxyId);
        else this.serviceProxy = new ServiceProxy(proxyId);

    }
   private byte[] sendRequest(byte[] request, TOMMessageType type) {
       try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

           Request deserialized = Request.deserialize(request);
           System.out.println(deserialized.getRequestType());
           List<Reply> replicaReplies = new LinkedList<>();
           if (!Security.verifySignature(deserialized.getPublicKey(), deserialized.getRequestType().toString().getBytes(), deserialized.getSignature())) {
               System.out.println("MAL ASSINADO PELO CLIENTE");
               Reply reply = new Reply("Request not properly signed!");
               replicaReplies.add(reply);
           } else {
               objOut.writeObject(deserialized);
               objOut.flush();
               byteOut.flush();
               if (this.async) {
                   System.out.println("ENTROU NO ASYNC");
                   BlockingQueue<List<Reply>> replyChain = new LinkedBlockingDeque<>();
                   ReplyListener replyListener = new ReplyListenerImp(replyChain, this.asyncServiceProxy);
                   this.asyncServiceProxy.invokeAsynchRequest(byteOut.toByteArray(), replyListener, type);
                   replicaReplies = replyChain.take();
                   if (replicaReplies.isEmpty())
                       replicaReplies.add(new Reply("Sem resposta"));
               } else {
                   System.out.println("ENTROU NO SYNC");
                   ByteArrayInputStream byteIn = new ByteArrayInputStream(serviceProxy.invoke(byteOut.toByteArray(), type));
                   ObjectInput objIn = new ObjectInputStream(byteIn);
                   Reply reply = Reply.deserialize((byte[]) objIn.readObject());
                   replicaReplies.add(reply);
               }
           }
           System.out.println("ADICIONAR RESPOSTAS A PROXY REPLY");
           ProxyReply proxyReply = new ProxyReply();
           replicaReplies.forEach(rep -> {
               System.out.println(rep!=null);
               System.out.println(rep.getError()==null);
               rep.setPublicKeyProxy(this.keyPair.getPublic().getEncoded());
               rep.setSignatureProxy(Security.signRequest(this.keyPair.getPrivate(), deserialized.getRequestType().toString().getBytes()));
               proxyReply.addReply(rep);
           });
           return ProxyReply.serialize(proxyReply);
       } catch (InterruptedException | IOException | ClassNotFoundException e) {
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
