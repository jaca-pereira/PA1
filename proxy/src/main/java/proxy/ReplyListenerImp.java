package proxy;

import Security.Security;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;
import data.Reply;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.synchronizedCollection;

public class ReplyListenerImp implements bftsmart.communication.client.ReplyListener {

    private AsynchServiceProxy asynchServiceProxy;

    private final Collection<Reply> replies =
            synchronizedCollection(new LinkedList<>());
    private final BlockingQueue<List<Reply>> replyChain;
    private AtomicInteger repliesCounter;

    public ReplyListenerImp(BlockingQueue<List<Reply>> replyChain, AsynchServiceProxy asynchServiceProxy) {
        System.out.println("CONSTRUTOR");
        this.replyChain = replyChain;
        this.asynchServiceProxy = asynchServiceProxy;
        repliesCounter = new AtomicInteger(0);
        System.out.println("CONSTRUTOR NÃO ESTOIROU");
    }

    @Override
    public void reset() {
        this.replyChain.clear();
        repliesCounter = new AtomicInteger(0);
    }

    @Override
    public void replyReceived(RequestContext context, TOMMessage msg) {
        System.out.println("REPLY_LISTENER_CHAMADO");
        if(msg.getContent().length > 0) {
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(msg.getContent());
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Reply reply = (Reply) objIn.readObject();
                if (!Security.verifySignature(reply.getPublicKeyReplica(), reply.getRequestType().toString().getBytes(), reply.getSignatureReplica())) {
                    replyChain.add(new LinkedList<>());
                    System.out.println("REPLICAS ASSINARAM MAL");
                    asynchServiceProxy.cleanAsynchRequest(context.getOperationId());
                    replies.clear();
                } else
                    replies.add(reply);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (this.isValid()) {
            replyChain.add(new LinkedList<>(replies));
            System.out.println("RESPOSTA READY");
            asynchServiceProxy.cleanAsynchRequest(context.getOperationId());
            replies.clear();
        }
    }

    private boolean isValid() {
        double quorum = (Math.ceil((double) (asynchServiceProxy.getViewManager().getCurrentViewN() + //4
                asynchServiceProxy.getViewManager().getCurrentViewF() + 1) / 2.0));
        repliesCounter.incrementAndGet();
        return repliesCounter.get() >=quorum;
    }
}
