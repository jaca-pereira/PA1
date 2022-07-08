package data;



import java.io.*;

import java.util.List;
import java.util.Map;

public class Block implements Serializable {


    private byte[] signature;
    private byte[] publicKey;

    private static final long serialVersionUID = 1L;

    private byte[] hash;

    private BlockHeader blockHeader;
    private int difficulty;

    public Block(int difficulty) {
        this.signature = null;
        this.publicKey = null;
        this.hash = null;
        this.difficulty = difficulty;
        this.blockHeader = null;
    }
    public Block(byte[] lastBlockHash, List<Transaction> transactionsList, Map<String, Account> transactionsMap, int difficulty) {
        this.signature = null;
        this.publicKey = null;
        this.hash = null;
        this.difficulty = difficulty;
        this.blockHeader = new BlockHeader(lastBlockHash, transactionsList, transactionsMap);
    }


    public static byte[] serialize(Block obj) {
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

    public static Block deserialize(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return (Block) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
