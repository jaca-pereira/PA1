package data;

import Security.Security;
import bftsmart.tom.util.TOMUtil;

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

    public List<Block> getMineratedBlocks() {
        return mineratedBlocks;
    }


    public int getBalance(byte[] account) {
        Account acc = notMineratedTransactionsMap.get(new String(account));
        if (acc == null)
            throw new IllegalArgumentException("Account does not exist!");
        return acc.getBalance();
    }

    public List<Transaction> getExtract(String id) {
        List<Transaction> extract = new LinkedList<>();
        for(Block block: this.mineratedBlocks)
            extract.addAll(block.getExtract(id));
        extract.addAll(this.notMineratedTransactionsMap.get(id).getTransactionList());
        return extract;
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
        int firstTransactionToMinerate = this.alreadyBeingMinerated;
        int lastTransactionToMinerate = this.alreadyBeingMinerated + MINIMUM_TRANSACTIONS;
        if (lastTransactionToMinerate > this.notMineratedTransactionsList.size()) {
            lastTransactionToMinerate = this.notMineratedTransactionsList.size();
            this.alreadyBeingMinerated = 0;
        } else this.alreadyBeingMinerated += MINIMUM_TRANSACTIONS;
        return this.notMineratedTransactionsList.subList(firstTransactionToMinerate, lastTransactionToMinerate);
    }

    public byte[] getLastMinedBlock() {
        return TOMUtil.computeHash(Block.serialize(this.mineratedBlocks.get(this.mineratedBlocks.size()-1)));
    }

    public boolean addMineratedBlock(Block block) {
    //verificar se bloco foi bem minerado
        if(!Security.verifySignature(block.getPublicKey(), block.getMerkle().getTransactionsHash(), block.getSignature()))
            throw new IllegalArgumentException("Block Signature not valid!");
        if (!Block.proofOfWork(block))
            throw new IllegalArgumentException("Block does not have proof of work!");
        List<Transaction> transactionsMinerated = block.getMerkle().getMerkelTree();
        if(this.notMineratedTransactionsList.removeAll(transactionsMinerated)) {
            this.mineratedBlocks.add(block);
            this.notMineratedTransactionsMap.forEach((accountId, account) -> account.removeTransactions(transactionsMinerated));
            return true;
        } else return false;

    }
}
