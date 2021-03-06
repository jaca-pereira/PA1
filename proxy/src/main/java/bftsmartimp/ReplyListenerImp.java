package bftsmartimp;

import Security.Security;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;
import data.Reply;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.security.PublicKey;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.synchronizedCollection;

public class ReplyListenerImp implements bftsmart.communication.client.ReplyListener {

    private AsynchServiceProxy asynchServiceProxy;

    private Collection<Reply> replies =
            synchronizedCollection(new LinkedList<>());
    private final BlockingQueue<List<Reply>> replyChain;
    private AtomicInteger repliesCounter;

    public ReplyListenerImp(BlockingQueue<List<Reply>> replyChain, AsynchServiceProxy asynchServiceProxy) {
        this.replyChain = replyChain;
        this.asynchServiceProxy = asynchServiceProxy;
        repliesCounter = new AtomicInteger(0);
    }

    @Override
    public void reset() {
        this.replyChain.clear();
        repliesCounter = new AtomicInteger(0);
        replies = synchronizedCollection(new LinkedList<>());
    }

    @Override
    public void replyReceived(RequestContext context, TOMMessage msg) {
        if(msg.getContent().length > 0) {
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(msg.getContent());
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Reply reply = (Reply) objIn.readObject();
                byte[] signature = reply.getSignatureReplica();
                PublicKey publicKey = reply.getPublicKeyReplica();
                if (publicKey == null || signature == null || !Security.verifySignature(publicKey, reply.getRequestType().toString().getBytes(), signature)) {
                    System.out.println("REPLY LIST NAO ASSINARAM BEM");
                    replies.clear();
                    replyChain.add(new LinkedList<>(replies));
                    asynchServiceProxy.cleanAsynchRequest(context.getOperationId());
                } else {
                    System.out.println("REPLY LIST ASSINARAM BEM");
                    replies.add(reply);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.isValid()) {
            System.out.println("REPLY LIST TEM QUORUM");
            replyChain.add(new LinkedList<>(replies));
            asynchServiceProxy.cleanAsynchRequest(context.getOperationId());
        }
    }

    private boolean isValid() {
        double quorum = (Math.ceil((double) (asynchServiceProxy.getViewManager().getCurrentViewN() + //4
                asynchServiceProxy.getViewManager().getCurrentViewF() + 1) / 2.0));
        repliesCounter.incrementAndGet();
        return repliesCounter.get() >=quorum;
    }
}
