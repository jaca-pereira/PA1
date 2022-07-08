package data;


import java.io.*;

import java.util.List;
import java.util.Map;

public class Merkle implements Serializable {

    public static final int SHA_256_SIZE = 256;
    private List<Transaction> merkleTree;
    private Map<String, Account> merkleMap;

    private byte[] transactionsHash;

    public Merkle(List<Transaction> merkelTree, Map<String, Account> merkelMap) {
        this.merkleTree = merkelTree;
        this.merkleMap = merkelMap;
        this.transactionsHash = null;
    }

    public static byte[] serialize(Merkle obj) {
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

    public static Merkle deserialize(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return (Merkle) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
