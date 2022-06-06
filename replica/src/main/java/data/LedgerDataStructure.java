package data;

import Security.Security;

import java.util.*;

public class LedgerDataStructure {


    public static final int MINIMUM_TRANSACTIONS = 10;
    private Map<String, Account> notMineratedTransactionsMap;
    private List<Transaction> notMineratedTransactionsList;
    private List<byte[]> transactionsId;
    private int globalValue;
    private List<Block> mineratedBlocks;
    private int alreadyBeingMinerated;

    public LedgerDataStructure() {
        this.notMineratedTransactionsMap = new HashMap<>();
        this.notMineratedTransactionsList = new LinkedList<>();
        this.transactionsId = new LinkedList<>();
        this.globalValue = 0;
        this.mineratedBlocks = new LinkedList<>();
        this.alreadyBeingMinerated = 0;
    }

    public void addAccount(byte[] account) {
        String acc = new String(account);
        if(this.notMineratedTransactionsMap.containsKey(acc))
            throw new IllegalArgumentException("Account already exists!");
        this.notMineratedTransactionsMap.put(acc, new Account());
    }

    public void transaction(Transaction t) {
        Account destinationAccount = this.notMineratedTransactionsMap.get(new String(t.getDestinationAccount()));
        if (destinationAccount == null)
            throw new IllegalArgumentException("Destination account does not exist!");
        if (t.getNonce()!=-1) {
            Account originAccount = this.notMineratedTransactionsMap.get(new String(t.getOriginAccount()));
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
        this.notMineratedTransactionsList.add(t);
    }


    public int getGlobalValue() {
        return this.globalValue;
    }

    //tem que se alterar
    public List<Transaction> getLedger() {
        return this.notMineratedTransactionsList;
    }

    public List<Block> getMineratedBlocks() {
        return mineratedBlocks;
    }


    public int getBalance(byte[] account) {
        Account acc = notMineratedTransactionsMap.get(new String(account));
        if (acc == null)
            throw new IllegalArgumentException("Account does not exist!");
        return acc.getBalance();
    }

    public List<Transaction> getExtract(byte[] account) {
        Account acc = notMineratedTransactionsMap.get(new String(account));
        if (acc == null)
            throw new IllegalArgumentException("Account does not exist!");
        return acc.getTransactionList();
    }

    public int getTotalValue(List<byte[]> accounts) {
        int totalValue = 0;
        for (byte[] account: accounts) {
            Account acc = notMineratedTransactionsMap.get(new String(account));
            if (acc == null)
                throw new IllegalArgumentException("Account "+ new String(account) + " does not exist!");
            totalValue += acc.getBalance();
        }
        return totalValue;
    }


    public List<Transaction> getTransactionsToMinerate() {
        if (notMineratedTransactionsList.size() < MINIMUM_TRANSACTIONS) {
            return null;
        }
        List<Transaction> transactionsToMinerate = new LinkedList();
        for (int i = 0; i < MINIMUM_TRANSACTIONS; i++) {
            Random r = new Random();
            int low = 0;
            int high = this.notMineratedTransactionsList.size();
            int result = r.nextInt(high-low) + low;
            transactionsToMinerate.add(this.notMineratedTransactionsList.get(result));
        }
        return transactionsToMinerate;
    }



    public void addMineratedBlock(Block block) {
    //verificar se bloco foi bem minerado
        if(!Security.verifySignature(block.getPublicKey(), block.getMerkle().getTransactionsHash(), block.getSignature()))
            throw new IllegalArgumentException("Block Signature not valid!");
        if (!Block.proofOfWork(block))
            throw new IllegalArgumentException("Block does not have proof of work!");
        this.mineratedBlocks.add(block);
        List<Transaction> transactionsMinerated = block.getMerkle().getMerkelTree();
        this.notMineratedTransactionsList.removeAll(transactionsMinerated);
        this.notMineratedTransactionsMap.forEach((accountId, account) -> account.removeTransactions(transactionsMinerated));
    }
}
