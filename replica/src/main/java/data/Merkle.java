package data;

import java.util.List;
import java.util.Map;

public class Merkle {

    private List<Transaction> merkelTree;
    private Map<String, Account> merkelMap;

    public Merkle(List<Transaction> merkelTree, Map<String, Account> merkelMap) {
        this.merkelTree = merkelTree;
        this.merkelMap = merkelMap;
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
}
