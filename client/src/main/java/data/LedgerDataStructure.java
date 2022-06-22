package data;

import Security.Security;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LedgerDataStructure {


    public static final int MINIMUM_TRANSACTIONS = 16;
    public static final int REWARD = 10;
    private Map<String, Account> notMinedTransactionsMap;
    private List<Transaction> notMinedTransactionsList;
    private List<byte[]> transactionsId;
    private int globalValue;
    private List<Block> minedBlocks;
    private int alreadyBeingMinerated;

    public LedgerDataStructure() {
        this.notMinedTransactionsMap = new HashMap<>();
        this.notMinedTransactionsList = new LinkedList<>();
        this.transactionsId = new LinkedList<>();
        this.globalValue = 0;
        this.minedBlocks = new LinkedList<>();
        this.alreadyBeingMinerated = 0;
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
        List<Transaction> extract = new LinkedList<>();
        for(Block block: this.minedBlocks)
            extract.addAll(block.getExtract(account));
        extract.addAll(acc.getTransactionList());
        return extract;
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


    public Block getBlockToMine() throws Exception {
        if (notMinedTransactionsList.size() < MINIMUM_TRANSACTIONS) {
            throw new Exception("Not enough transactions no mine!");
        }
        List<Transaction> transactions = this.notMinedTransactionsList.subList(0,9);
        Map<String, Account> map = new HashMap<>();

        for (Transaction transaction : transactions) {
            String account = new String(transaction.getOriginAccount());
            map.put(account, this.notMinedTransactionsMap.get(account));
        }
        return new Block(this.minedBlocks.get(this.minedBlocks.size()-1).getBlockHash(), transactions, map);
    }

    public int addMinedBlock(Block block) throws Exception {
        if(!Security.verifySignature(block.getPublicKey(), block.getMerkle().getTransactionsHash(), block.getSignature()))
            throw new IllegalArgumentException("Block Signature not valid!");
        if (!Block.proofOfWork(block))
            throw new IllegalArgumentException("Block does not have proof of work!");
        List<Transaction> transactionsMined = block.getMerkle().getMerkelTree();
        if(this.notMinedTransactionsList.removeAll(transactionsMined)) {
            this.minedBlocks.add(block);
            this.notMinedTransactionsMap.forEach((accountId, account) -> account.removeTransactions(transactionsMined));
            Account miner = this.notMinedTransactionsMap.get(block.getAccount());
            miner.changeBalance(REWARD);
            return miner.getBalance();
        } else throw new Exception("Block already mined!");

    }

}
