package data;

import bftsmart.tom.util.TOMUtil;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Merkle {

    public static final int SHA_256_SIZE = 256;
    private List<Transaction> merkelTree;
    private Map<String, Account> merkelMap;

    private byte[] transactionsHash;

    public Merkle(List<Transaction> merkelTree, Map<String, Account> merkelMap) {
        this.merkelTree = merkelTree;
        this.merkelMap = merkelMap;
        this.transactionsHash = this.setTransactionsHash();
    }

    public List<Transaction> getExtract(String id) {
        Account account = merkelMap.get(id);
        return account.getTransactionList();
    }

    public List<Transaction> getMerkelTree() {
        return this.merkelTree;
    }

    public Map<String, Account> getMerkelMap() {
        return this.merkelMap;
    }

    public byte[] getTransactionsHash (){
        return this.transactionsHash;
    }

    private byte[] setTransactionsHash() {
        if (merkelTree.size()==0)
            return new byte[]{0x0};

        List<byte[]> hash1 = new LinkedList<>();
        List<byte[]> hash2 = new LinkedList<>();
        List<byte[]> hash = hash1;
        this.merkelTree.forEach(transaction -> hash.add(TOMUtil.computeHash(transaction.getId())));
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

}
