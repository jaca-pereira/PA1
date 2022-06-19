package data;

import bftsmart.tom.util.TOMUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;

public class Block {
    private byte[] lastBlockHash;
    private long nonce;
    private Merkle merkle;

    private byte[] signature;
    private PublicKey publicKey;

    private byte[] account;

    public Block(byte[] lastBlockHash, List<Transaction> transactionsList, Map<String, Account> transactionsMap) {
        this.lastBlockHash = lastBlockHash;
        this.nonce = -1;
        this.signature = null;
        this.publicKey = null;
        this.account = null;
        this.merkle = new Merkle(transactionsList, transactionsMap);
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void setAccount(byte[] account) {
        this.account = account;
    }

    public byte[] getAccount() {
        return account;
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

    public PublicKey getPublicKey() {
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

    public byte[] thisBlockHash() {
        return TOMUtil.computeHash(Block.serialize(this));
    }

    public static boolean proofOfWork(Block block) {
        try {
            byte[] blockHash = TOMUtil.computeHash(Block.serialize(block));
            int count = 0;
            byte[] number = {blockHash[blockHash.length-1], blockHash[blockHash.length-2]};
            for (int i = 0; i < blockHash.length-1; i++) {
                byte b = blockHash[i];
                if (b == number[count]) {
                    count++;
                    if (count == 2)
                        return true;
                } else {
                    count = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}