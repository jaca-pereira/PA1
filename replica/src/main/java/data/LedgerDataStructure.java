package data;

import Security.Security;

import java.util.*;

public class LedgerDataStructure {


    public static final int MAXIMUM_MINIMUM_TRANSACTIONS = 16;
    private final int difficulty;

    private List<Transaction> notMinedTransactionsList;
    private List<byte[]> transactionsId;
    private int globalValue;
    private List<Block> minedBlocks;

    private List<Block> blocksToMine;
    private Map<String,Integer> accounts;
    private int minimumTransactions;


    public LedgerDataStructure(int difficulty) {
        this.notMinedTransactionsList = new ArrayList<>(MAXIMUM_MINIMUM_TRANSACTIONS);
        this.transactionsId = new LinkedList<>();
        this.globalValue = 0;
        this.minedBlocks = new LinkedList<>();
        this.blocksToMine = new LinkedList<>();
        this.accounts = new HashMap<>();
        this.genesisBlock();
        this.minimumTransactions = 1;
        this.difficulty = difficulty;

    }

    private void genesisBlock() {
        Block genesis = new Block();
        this.blocksToMine.add(genesis);
    }

    public void addAccount(byte[] account) {
        String acc = new String(account);
        if(this.accounts.containsKey(acc))
            throw new IllegalArgumentException("Account already exists!");
        this.accounts.put(acc, 0);
    }

    public void transaction(Transaction t) {
        String dtAccount = new String(t.getDestinationAccount());
        if (dtAccount == null)
            throw new IllegalArgumentException("Destination account is null!");
        Integer balanceDestination = this.accounts.get(dtAccount);
        if (balanceDestination == null)
            throw new IllegalArgumentException("Destination account does not exist!");
        if (t.getNonce()!=-1) {
            String orAccount = new String(t.getOriginAccount());
            if (orAccount==null)
                throw new IllegalArgumentException("Origin account is null!");
            Integer balance = this.accounts.get(orAccount);
            if (balance == null)
                throw new IllegalArgumentException("Origin account does not exist!");
            if (this.transactionsId.contains(t.getId()))
                throw new IllegalArgumentException("Request already made! Byzantine attack?");
            if (balance < t.getValue())
                this.transactionsId.add(t.getId());
            else
                throw new IllegalArgumentException("Origin account does not have sufficient balance!");
        }
        this.notMinedTransactionsList.add(t);
        if (this.notMinedTransactionsList.size() == minimumTransactions)
            this.generateBlock();
    }

    private void generateBlock() {
        Block last = this.minedBlocks.get(minedBlocks.size()-1);
        Map<String, Account> merkleMap = new HashMap<>();
        List<Transaction> merkleTree = new LinkedList<>();
        for (Transaction transaction: this.notMinedTransactionsList ) {
            if (transaction.getNonce() != -1) { // se não for uma reward do ledger
                Account account = merkleMap.get(new String(transaction.getOriginAccount()));
                if (account == null) // se a conta nao estiver ja na nova merkle tree, criar
                    account = new Account();
                Integer balance = this.accounts.get(new String(transaction.getOriginAccount()));
                if (balance!= null) { // se a conta ja existe
                    account.changeBalance(balance);
                }
                if (account.getBalance() < transaction.getValue()) //se não tem saldo suficiente, saltar transação
                    continue;
                account.changeBalance(account.getBalance() - transaction.getValue());
                accounts.put(new String(transaction.getOriginAccount()),balance - transaction.getValue());
                merkleMap.put(new String(transaction.getOriginAccount()), account);
            } else { //aumentar o valor global do ledger
                this.globalValue += transaction.getValue();
            }
            Account destinationAccount = merkleMap.get(new String(transaction.getDestinationAccount()));
            if (destinationAccount == null)
                destinationAccount = new Account();
            Integer destinationBalance = this.accounts.get(new String(transaction.getDestinationAccount()));
            if (destinationBalance!= null) { // se a conta ja existe
                destinationAccount.changeBalance(destinationBalance);
            }
            destinationAccount.changeBalance(destinationAccount.getBalance() + transaction.getValue());
            accounts.put(new String(transaction.getDestinationAccount()),destinationBalance + transaction.getValue());
            merkleMap.put(new String(transaction.getDestinationAccount()), destinationAccount);

            merkleTree.add(transaction);
        }
        blocksToMine.add(new Block(last.getBlockHash(), merkleTree, merkleMap, difficulty));
        notMinedTransactionsList = new ArrayList<>(MAXIMUM_MINIMUM_TRANSACTIONS);
        if(this.minimumTransactions != MAXIMUM_MINIMUM_TRANSACTIONS)
            this.minimumTransactions *= 2;
    }


    public int getGlobalValue() {
        return this.globalValue;
    }



    public int getBalance(byte[] account) {
        Integer balance = accounts.get(new String(account));
        if (balance == null)
            throw new IllegalArgumentException("Account does not exist!");
        return balance;
    }

    public List<Transaction> getExtract(byte[] account, int begin) {
        if (!accounts.containsKey(new String(account)))
            throw new IllegalArgumentException("Account does not exist!");
        List<Transaction> extract = new LinkedList<>();
        for(Block block: this.minedBlocks)
            extract.addAll(block.getExtract(account));
        return extract;
    }

    public int getTotalValue(List<byte[]> accounts) {
        int totalValue = 0;
        for (byte[] account: accounts) {
            Integer balance = this.accounts.get(new String(account));
            if (balance == null)
                throw new IllegalArgumentException("Account does not exist!");
            totalValue += balance;
        }
        return totalValue;
    }


    public Block getBlockToMine() throws Exception {
        if (blocksToMine.size()==0)
            throw new Exception("No blocks to mine");
        return blocksToMine.get(0);
    }

    public void addMinedBlock(Block block) throws Exception {
        if(accounts.containsKey(new String(block.getAccount())))
            throw new IllegalArgumentException("Account does not exist");
        if(!Security.verifySignature(block.getPublicKey(), block.getMerkle().getTransactionsHash(), block.getSignature()))
            throw new IllegalArgumentException("Block Signature not valid!");
        if (!Block.proofOfWork(block))
            throw new IllegalArgumentException("Block does not have proof of work!");
        if(!new String(blocksToMine.get(0).getBlockHash()).equals(new String(block.getBlockHash())))
            throw new Exception("Block already mined!");
        blocksToMine.remove(0);
        minedBlocks.add(block);
    }

}
