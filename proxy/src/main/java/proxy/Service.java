package proxy;


import Security.Security;
import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;

import bftsmart.tom.core.messages.TOMMessageType;
import bftsmartimp.ReplyListenerImp;
import blockmess.ApplicationInterfaceImp;
import blockmess.ReplyListenerBlockmess;
import data.*;

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
    private ApplicationInterfaceImp blockmessInterface;
    private final boolean blockmess;
    private Ledger ledger;

    public Service(int proxyId, boolean blockmess, int numNodes, String address) {
        try {
            this.keyPair = Security.getKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerErrorException("Proxy key not created!");
        }
        this.blockmess = blockmess;
        if (blockmess) {
            this.ledger = new Ledger();
            String[] properties = new String[2];
            properties[0] = String.format("address=%s", address);
            properties[1] = String.format("expectedNumNodes=%s", numNodes);
            this.blockmessInterface = new ApplicationInterfaceImp(properties, ledger);
        }else
            this.asyncServiceProxy = new AsynchServiceProxy(proxyId);

    }

    private List<Reply> sendRequestBFTSmart(byte[] request, TOMMessageType type) throws InterruptedException {
       BlockingQueue<List<Reply>> replyChain = new LinkedBlockingDeque<>();
       ReplyListener replyListener = new ReplyListenerImp(replyChain, this.asyncServiceProxy);
       this.asyncServiceProxy.invokeAsynchRequest(request, replyListener,type);
       List<Reply> replicaReplies = replyChain.take();
       if (replicaReplies.isEmpty()) {
           System.out.println("NAO ASSINARAM BEM ");
           replicaReplies.add(new Reply("Replicas não assinaram bem."));
       }
       return replicaReplies;
    }
    private List<Reply> sendRequestBlockmess(byte[] bytes, Request request, TOMMessageType type) throws IOException, ClassNotFoundException {
        List<Reply> replies = new LinkedList<>();
        Reply reply;
        if (type.equals(TOMMessageType.ORDERED_REQUEST)) {
            if (!request.getRequestType().equals(LedgerRequestType.GET_BLOCK_TO_MINE)) {
                applicationInterface.ReplyListener replyListener = new ReplyListenerBlockmess();
                this.blockmessInterface.invokeAsyncOperation(bytes, replyListener);
                reply = new Reply(true, request.getRequestType());
            } else {
                ByteArrayInputStream byteIn = new ByteArrayInputStream(this.blockmessInterface.invokeSyncOperation(bytes).getLeft());
                ObjectInput objIn = new ObjectInputStream(byteIn);
                reply = (Reply) objIn.readObject();
            }

        } else {
            switch (request.getRequestType()) {
                case GET_BALANCE:
                    reply = new Reply(this.ledger.getBalance(request.getAccount()), request.getRequestType());
                    break;
                case GET_EXTRACT:
                    reply = new Reply(this.ledger.getExtract(request.getAccount()), request.getRequestType());
                    break;
                case GET_GLOBAL_VALUE:
                    reply = new Reply(this.ledger.getGlobalValue(), request.getRequestType());
                    break;
                case GET_LEDGER:
                    reply = new Reply(this.ledger.getLedger(), request.getRequestType());
                    break;
                case GET_TOTAL_VALUE:
                    reply = new Reply(this.ledger.getTotalValue(request.getAccounts()), request.getRequestType());
                    break;
                default:
                    reply = new Reply("Operation not suported");
            }
        }
        replies.add(reply);
        return replies;
    }

    private List<Reply> executeCommand(Request request, ByteArrayOutputStream byteOut, ObjectOutput objOut) throws InterruptedException, IOException, ClassNotFoundException {
        objOut.writeObject(request);
        objOut.flush();
        byteOut.flush();
        switch (request.getRequestType()) {
            case CREATE_ACCOUNT:
            case SEND_TRANSACTION:
            case MINE_BLOCK:
            case GET_BLOCK_TO_MINE:
                if (blockmess)
                   return this.sendRequestBlockmess(byteOut.toByteArray(), request, TOMMessageType.ORDERED_REQUEST);
               else {
                   System.out.println("entrou num dos writes");
                    return this.sendRequestBFTSmart(byteOut.toByteArray(), TOMMessageType.ORDERED_REQUEST);
                }

            case GET_BALANCE:
            case GET_EXTRACT:
            case GET_GLOBAL_VALUE:
            case GET_LEDGER:
            case GET_TOTAL_VALUE:
                if (blockmess)
                    return this.sendRequestBlockmess(byteOut.toByteArray(), request, TOMMessageType.UNORDERED_REQUEST);
                else {
                    return this.sendRequestBFTSmart(byteOut.toByteArray(), TOMMessageType.UNORDERED_REQUEST);
                }
            default:
                return new LinkedList<>();
        }
    }

    private byte[] sendRequest(byte[] request) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
            Request deserialized = Request.deserialize(request);
            List<Reply> replicaReplies = new LinkedList<>();
            if (!Security.verifySignature(deserialized.getPublicKey(), deserialized.getRequestType().toString().getBytes(), deserialized.getSignature())) {
                Reply reply = new Reply("Request not properly signed!");
                replicaReplies.add(reply);
            } else {
                replicaReplies = this.executeCommand(deserialized, byteOut, objOut);
            }
            ProxyReply proxyReply = new ProxyReply();
            replicaReplies.forEach(rep -> {
                rep.setPublicKeyProxy(this.keyPair.getPublic().getEncoded());
                rep.setSignatureProxy(Security.signRequest(this.keyPair.getPrivate(), deserialized.getRequestType().toString().getBytes()));
                proxyReply.addReply(rep);
            });
            return ProxyReply.serialize(proxyReply);
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] createAccount(byte[] request) {
        return this.sendRequest(request);
    }

    @Override
    public byte[] getBalance(byte[] request) {
        return this.sendRequest(request);
    }

    @Override
    public byte[] getExtract(byte[] request) {
        return this.sendRequest(request);
    }

    @Override
    public byte[] sendTransaction(byte[] request) {
        return this.sendRequest(request);
    }

    @Override
    public byte[] getTotalValue(byte[] request) {
        return this.sendRequest(request);
    }

    @Override
    public byte[] getGlobalValue(byte[] request) {
       return this.sendRequest(request);
    }

    @Override
    public byte[] getLedger(byte[] request) {
        return this.sendRequest(request);
    }

    @Override
    public byte[] getBlockToMine(byte[] request) {
       return this.sendRequest(request);
    }

    @Override
    public byte[] mineBlock(byte[] request) {
        return this.sendRequest(request);
    }

}
