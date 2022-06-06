package data;

import bftsmart.tom.util.TOMUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

public class Block {
    private byte[] lastBlockHash;
    private long nonce;
    private Merkle merkle;

    private byte[] signature;
    private byte[] publicKey;
    private byte[] account;

    public Block(byte[] lastBlockHash, long nonce, byte[] signature, byte[] publicKey, byte[] account, List<Transaction> transactions, Map<String, Account> map) {
        this.lastBlockHash = lastBlockHash;
        this.nonce = nonce;
        this.signature = signature;
        this.publicKey = publicKey;
        this.account = account;
        this.merkle = new Merkle(transactions, map);
    }

    public byte[] getLastBlockHash() {
        return this.lastBlockHash;
    }

    public List<Transaction> getExtract(String id) {
        return this.merkle.getExtract(id);
    }

    public Merkle getMerkle() {
        return this.merkle;
    }

    public long getNonce() {
        return this.nonce;
    }

    public byte[] getSignature() {
        return signature;
    }

    public byte[] getPublicKey() {
        return publicKey;
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

    public static boolean proofOfWork(Block block) {
        try {
            byte[] blockHash = TOMUtil.computeHash(Block.serialize(block));
            System.out.println();
            int count = 0;
            byte[] number = {blockHash[blockHash.length-1], blockHash[blockHash.length-2]};
            for (int i = 0; i < blockHash.length-1; i++) {
                byte b = blockHash[i];
                if (b == number[count]) {
                    count++;
                    if (count == 2)
                        return true;
                } else {
                    count--;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
