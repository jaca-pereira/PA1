package data;

import bftsmart.tom.util.TOMUtil;

import java.io.*;
import java.math.BigInteger;
import java.security.PublicKey;

import java.util.List;
import java.util.Map;

public class Block implements Serializable {
    private byte[] lastBlockHash;
    private long nonce;
    private Merkle merkle;

    private byte[] signature;
    private byte[] publicKey;

    private byte[] account;

    private static final long serialVersionUID = 1L;

    private byte[] hash;

    public Block(byte[] lastBlockHash, List<Transaction> transactionsList, Map<String, Account> transactionsMap) {
        this.lastBlockHash = lastBlockHash;
        this.nonce = -1;
        this.signature = null;
        this.publicKey = null;
        this.account = null;
        this.merkle = new Merkle(transactionsList, transactionsMap);
        this.hash = null;
    }


    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public void setPublicKey(byte[] publicKey) {
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

    public List<Transaction> getExtract(byte[] account) {
        return this.merkle.getExtract(new String(account));
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

    public byte[] getHash() {
        return this.hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public static boolean proofOfWork(Block block) {
        try {
            BigInteger hashInt = new BigInteger(block.getLastBlockHash());
            long nonceHash = block.getNonce() + hashInt.longValue();
            if(nonceHash < Long.MAX_VALUE)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String toString() {
        String res = "Last Block Hash: " + new String(lastBlockHash) + "\n";
        res += "Nonce: " + this.nonce + "\n";
        res += "Merkle: {" + this.merkle.toString() + "}\n";
        res += "Signature: " + new String(signature) + "\n";
        res += "Public Key: " + new String(publicKey) + "\n";
        res += "Account: " + new String(account) + "\n";
        res += "Hash: " + new String(hash) + "\n";
        return res;
    }



}
