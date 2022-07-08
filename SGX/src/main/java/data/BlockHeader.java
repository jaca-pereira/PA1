package data;

import java.io.*;
import java.util.List;
import java.util.Map;

public class BlockHeader implements Serializable {
    private byte[] lastBlockHash;
    private long nonce;
    private Merkle merkle;
    private byte[] account;
    private static final long serialVersionUID = 1L;

    public BlockHeader(byte[] lastBlockHash, List<Transaction> transactionsList, Map<String, Account> transactionsMap) {
        this.lastBlockHash = lastBlockHash;
        this.nonce = -1;
        this.account = null;
        this.merkle = new Merkle(transactionsList, transactionsMap);
    }

    public byte[] getLastBlockHash() {
        return lastBlockHash;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public byte[] getAccount() {
        return account;
    }

    public void setAccount(byte[] account) {
        this.account = account;
    }


    public List<Transaction> getExtract(String account) {
        return this.merkle.getExtract(account);
    }

    public String toString() {
        String res = "Last Block Hash: " + new String(this.lastBlockHash);
        res += "Nonce: " + this.nonce + "\n";
        res += "Merkle: {" + this.merkle.toString() + "}\n";
        res += "Account: " + new String(account) + "\n";
        return res;
    }

    public static byte[] serialize(BlockHeader obj) {
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

    public static BlockHeader deserialize(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return (BlockHeader) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
