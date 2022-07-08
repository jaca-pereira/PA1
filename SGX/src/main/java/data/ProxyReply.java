package data;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class ProxyReply implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<byte[]> replicaReplies;

    public ProxyReply() {
        this.replicaReplies = new LinkedList<>();
    }

    public List<byte[]> getReplicaReplies() {
        return this.replicaReplies;
    }

    public void addReply(Reply rep) {
        this.replicaReplies.add(Reply.serialize(rep));
    }


    public static byte[] serialize(ProxyReply obj) {
        try {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(obj);
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static ProxyReply deserialize(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return (ProxyReply) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
