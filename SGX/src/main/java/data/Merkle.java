package data;

import bftsmart.tom.util.TOMUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.LinkedList;
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
        this.transactionsHash = this.setTransactionsHash();
    }

    public List<Transaction> getExtract(String id) {
        Account account = merkleMap.get(id);
        if (account == null)
            return new LinkedList<>();
        return account.getTransactionList();
    }

    public List<Transaction> getMerkleTree() {
        return this.merkleTree;
    }

    public Map<String, Account> getMerkleMap() {
        return this.merkleMap;
    }

    public byte[] getTransactionsHash (){
        return this.transactionsHash;
    }

    private byte[] setTransactionsHash() {
        if (merkleTree.size()==0)
            return new byte[]{0x0};

        List<byte[]> hash1 = new LinkedList<>();
        List<byte[]> hash2 = new LinkedList<>();
        List<byte[]> hash = hash1;
        this.merkleTree.forEach(transaction -> hash.add(TOMUtil.computeHash(transaction.getId())));
        hash1 = hash;
        while (hash1.size() > 1) {
            for (int i = 0; i < hash1.size(); i += 2) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(SHA_256_SIZE*2);
                byteBuffer.put(hash1.get(i));
                if(i+1<hash1.size())
                    byteBuffer.put(hash1.get(i+1));
                hash2.add(TOMUtil.computeHash(byteBuffer.array()));
            }
            hash1 = hash2;
            hash2 = new LinkedList<>();
        }
        return hash1.get(0);
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

    public String toString() {
        String result = "Merkle Tree: {\n";
        for (Transaction t : merkleTree) {
            result += t.toString();
        }
        result+= "}\n";
        result += "Merkle Map: {\n";
        for (String account: merkleMap.keySet()) {
            result += "account: " + account + "-> " + merkleMap.get(account).toString();
        }
        result+= "}\n";
        result+= "Transactions Hash: " + new String(transactionsHash) +"\n";
        return result;
    }
}
