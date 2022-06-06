package data;

import bftsmart.tom.util.TOMUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Block {
    private byte[] lastBlockHash;
    private long nonce;
    private List<Transaction> merkelTree;
    private Map<String, Account> merkelMap;

    private byte[] signature;
    private byte[] publickKey;
    private byte[] account;

    public Block(byte[] lastBlockHash, long nonce, byte[] signature, byte[] publickKey, byte[] account) {
        this.lastBlockHash = lastBlockHash;
        this.nonce = nonce;
        this.merkelTree = new LinkedList<>();
        this.merkelMap = new HashMap<>();
        this.signature = signature;
        this.publickKey = publickKey;
        this.account = account;
    }

    public void addTransaction(Transaction transaction) {
        this.merkelTree.add(transaction);

    }

    public byte[] getLastBlockHash() {
        return this.lastBlockHash;
    }

    public List<Transaction> getMerkelTree() {
        return this.merkelTree;
    }

    public long getNonce() {
        return this.nonce;
    }

    public byte[] getSignature() {
        return signature;
    }

    public byte[] getPublickKey() {
        return publickKey;
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

    public static byte[] hash(byte[] serializedBlock) {
        return TOMUtil.computeHash(serializedBlock);
    }

}
