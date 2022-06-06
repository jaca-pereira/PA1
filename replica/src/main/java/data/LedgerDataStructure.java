package data;

import java.util.*;

public class LedgerDataStructure {


    private Map<String, Account> notMinedTransactionsMap;
    private List<Transaction> notMinedTransactionsList;
    private List<byte[]> transactionsId;
    private int globalValue;
    private List<Block> minedBlocks;

    public LedgerDataStructure() {
        this.notMinedTransactionsMap = new HashMap<>();
        this.notMinedTransactionsList = new LinkedList<>();
        this.transactionsId = new LinkedList<>();
        this.globalValue = 0;
        this.minedBlocks = new LinkedList<>();
    }

    public void addAccount(byte[] account) {
        String acc = new String(account);
        if(this.notMinedTransactionsMap.containsKey(acc))
            throw new IllegalArgumentException("Account already exists!");
        this.notMinedTransactionsMap.put(acc, new Account());
    }

    public void transaction(Transaction t) {
        Account destinationAccount = this.notMinedTransactionsMap.get(new String(t.getDestinationAccount()));
        if (destinationAccount == null)
            throw new IllegalArgumentException("Destination account does not exist!");
        if (t.getNonce()!=-1) {
            Account originAccount = this.notMinedTransactionsMap.get(new String(t.getOriginAccount()));
            if (originAccount == null)
                throw new IllegalArgumentException("Origin account does not exist!");
            if (this.transactionsId.contains(t.getId()))
                throw new IllegalArgumentException("Request already made! Byzantine attack?");
            if (originAccount.getBalance()>=t.getValue()) {
                this.transactionsId.add(t.getId());
                originAccount.addTransaction(t);
                originAccount.changeBalance(-t.getValue());
            }else
                throw new IllegalArgumentException("Origin account does not have sufficient balance!");
        } else
            this.globalValue += t.getValue();
        destinationAccount.addTransaction(t);
        destinationAccount.changeBalance(t.getValue());
        this.notMinedTransactionsList.add(t);
    }


    public int getGlobalValue() {
        return this.globalValue;
    }

    //tem que se alterar
    public List<Transaction> getLedger() {
        return this.notMinedTransactionsList;
    }

    public List<Block> getMinedBlocks() {
        return minedBlocks;
    }


    public int getBalance(byte[] account) {
        Account acc = notMinedTransactionsMap.get(new String(account));
        if (acc == null)
            throw new IllegalArgumentException("Account does not exist!");
        return acc.getBalance();
    }

    public List<Transaction> getExtract(byte[] account) {
        Account acc = notMinedTransactionsMap.get(new String(account));
        if (acc == null)
            throw new IllegalArgumentException("Account does not exist!");
        return acc.getTransactionList();
    }

    public int getTotalValue(List<byte[]> accounts) {
        int totalValue = 0;
        for (byte[] account: accounts) {
            Account acc = notMinedTransactionsMap.get(new String(account));
            if (acc == null)
                throw new IllegalArgumentException("Account "+ new String(account) + " does not exist!");
            totalValue += acc.getBalance();
        }
        return totalValue;
    }
}
