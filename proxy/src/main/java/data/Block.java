package data;

import bftsmart.tom.util.TOMUtil;

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

    public Block(byte[] lastBlockHash, List<Transaction> transactionsList, Map<String, Account> transactionsMap, int difficulty) {
        this.signature = null;
        this.publicKey = null;
        this.hash = null;
        this.difficulty = difficulty;
        this.blockHeader = new BlockHeader(lastBlockHash, transactionsList, transactionsMap);
    }

    public int getDifficulty() {
        return difficulty;
    }
    public void setNonce(long nonce) {
        this.blockHeader.setNonce(nonce);
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public void setAccount(byte[] account) {
        this.blockHeader.setAccount(account);
    }

    public byte[] getAccount() {
        return this.blockHeader.getAccount();
    }
    public byte[] getLastBlockHash() {
        return this.blockHeader.getLastBlockHash();
    }

    public List<Transaction> getExtract(byte[] account) {
        return this.blockHeader.getExtract(new String(account));
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

    public byte[] getHash() {
        return this.hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public static boolean proofOfWork(Block block) {
        try {
            byte[] hash = TOMUtil.computeHash(BlockHeader.serialize(block.getHeader()));
            Byte zero = Byte.valueOf("0");
            int count = 0;
            for (int i = 0; i < hash.length; i++) {
                String bt = String.valueOf(hash[i]);
                if (bt.equals(String.valueOf(zero)))
                    count++;
                else count=0;
                if(count >= block.getDifficulty())
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public BlockHeader getHeader() {
        return this.blockHeader;
    }

    public String toString() {
        String res = this.blockHeader.toString();
        res += "Signature: " + new String(signature) + "\n";
        res += "Public Key: " + new String(publicKey) + "\n";
        res += "Hash: " + new String(hash) + "\n";
        return res;
    }

}
