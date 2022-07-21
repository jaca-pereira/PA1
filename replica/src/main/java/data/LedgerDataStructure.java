package data;

import Security.Security;
import bftsmart.tom.util.TOMUtil;

import java.io.*;
import java.util.*;

public class LedgerDataStructure implements Serializable{

    private static final long serialVersionUID = 1L;
    public static final int MAXIMUM_MINIMUM_TRANSACTIONS = 8;
    public static final int DIFFICULTY = 4;

    private List<Transaction> notMinedTransactionsList;

    private int globalValue;
    private List<Block> minedBlocks;

    private Map<String,Integer> accounts;
    private int minimumTransactions;
    private int difficulty;

    private Block blockToMine;


    public LedgerDataStructure() {
        this.notMinedTransactionsList = new LinkedList<>();
        this.globalValue = 0;
        this.minedBlocks = new LinkedList<>();
        this.accounts = new HashMap<>();
        this.minimumTransactions = 1;
        this.difficulty = 0;
        this.blockToMine = genesisBlock();
    }

    private Block genesisBlock() {
        return new Block(new byte[]{0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0}, new LinkedList<>(), new HashMap<>(), this.difficulty);
    }

    public void addAccount(byte[] account) {
        String acc = new String(account);
        if(this.accounts.get(acc)!=null){
            throw new IllegalArgumentException("Account already exists!");
        }

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
            if (balance < t.getValue())
                throw new IllegalArgumentException("Origin account does not have sufficient balance!");
        }
        System.out.println("ADICIONOU TRANSAÇÃO :   " + t.getValue());
        this.notMinedTransactionsList.add(t);
    }

    private Block generateBlock() {
        Map<String, Account> merkleMap = new HashMap<>();
        List<Transaction> merkleTree = new LinkedList<>();
        System.out.println("ENTROU NO GENERATE BLOCK");
        for (int i=0; i < minimumTransactions; i++) {
            Transaction transaction =this.notMinedTransactionsList.remove(0);
            System.out.println("ANDOU PELAS TRANSAÇÕES");
            if (transaction.getNonce() != -1) { // se não for uma reward do ledger
                Integer balance = this.accounts.get(new String(transaction.getOriginAccount()));
                if (balance== null)
                    continue; //conta não existe
                Account account = merkleMap.get(new String(transaction.getOriginAccount()));
                if (account == null) // se a conta nao estiver ja na nova merkle tree, criar
                    account = new Account();
                account.changeBalance(balance);
                if (account.getBalance() < transaction.getValue()) //se não tem saldo suficiente, saltar transação
                    continue; //conta não tem saldo
                account.changeBalance(account.getBalance() - transaction.getValue());
                account.addTransaction(transaction);
                accounts.put(new String(transaction.getOriginAccount()),account.getBalance());
                merkleMap.put(new String(transaction.getOriginAccount()), account);
            }
            Integer destinationBalance = this.accounts.get(new String(transaction.getDestinationAccount()));
            if (destinationBalance == null)
                continue; //conta nao existe
            Account destinationAccount = merkleMap.get(new String(transaction.getDestinationAccount()));
            if (destinationAccount == null)
                destinationAccount = new Account();
            destinationAccount.changeBalance(destinationBalance);
            destinationAccount.changeBalance(destinationAccount.getBalance() + transaction.getValue());
            destinationAccount.addTransaction(transaction);
            accounts.put(new String(transaction.getDestinationAccount()),destinationAccount.getBalance());
            merkleMap.put(new String(transaction.getDestinationAccount()), destinationAccount);
            merkleTree.add(transaction);
            if (transaction.getNonce() == -1)
                this.globalValue += transaction.getValue();
        }
        byte[] lastBlockHash= this.minedBlocks.get(minedBlocks.size()-1).getHash();
        if (this.minedBlocks.size()>=2) {
            if (this.difficulty != DIFFICULTY)
                this.difficulty = DIFFICULTY;
        }
        if(this.minimumTransactions != MAXIMUM_MINIMUM_TRANSACTIONS)
            this.minimumTransactions *=2;
        return new Block(lastBlockHash, merkleTree, merkleMap, this.difficulty);
    }


    public int getGlobalValue() {
        return this.globalValue;
    }



    public int getBalance(byte[] account) {
        Integer balance = accounts.get(new String(account));
        if (balance == null){
            throw new IllegalArgumentException("Account does not exist!");
        }
        return balance;
    }

    public List<Transaction> getExtract(byte[] account) {
        if (accounts.get(new String(account))==null){
            throw new IllegalArgumentException("Account does not exist!");
        }
        List<Transaction> extract = new LinkedList<>();
        for(Block block: this.minedBlocks)
            extract.addAll(block.getExtract(account));
        return extract;
    }

    public int getTotalValue(List<byte[]> accounts) {
        int totalValue = 0;
        for (byte[] account: accounts) {
            Integer balance = this.accounts.get(new String(account));
            if (balance == null) {
                throw new IllegalArgumentException("Account does not exist!");
            }
            totalValue += balance;
        }
        return totalValue;
    }


    public Block getBlockToMine() throws Exception {
        System.out.println("ENTROU NO BLOCK TO MINE");
        if (blockToMine.getDifficulty()==-1) {
            if (this.notMinedTransactionsList.size()>=this.minimumTransactions) {
                blockToMine = this.generateBlock();
                System.out.println("GEROU BLOCO");
            } else {
                System.out.println("NÃO HA BLOCOS PARA MINAR");
                throw new Exception("NO BLOCKS TO MINE");
            }
        }
        return blockToMine;
    }

    public void addMinedBlock(Block block) throws Exception {
        if (accounts.get(new String(block.getAccount()))==null) {
            System.out.println("CONTA NULL");
            throw new IllegalArgumentException("Account does not exist");
        } else if (!Block.proofOfWork(block)) {
            System.out.println("bloco nao pow");
            throw new IllegalArgumentException("Block does not have proof of work!");
        } else if(blockToMine == null || !new String(blockToMine.getLastBlockHash()).equals(new String(block.getLastBlockHash()))) {
            System.out.println("BLOCO JA FOI MINADO OU HASH NAO É IGUAL");
            throw new Exception("Block already mined!");
        }
        minedBlocks.add(block);
        System.out.println(minedBlocks.size());
        this.blockToMine = new Block(-1); //bloco para assinalar que não existe
    }

    public List<Block> getLedger() {
        return minedBlocks;
    }

    public static byte[] serialize(LedgerDataStructure obj) {
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

    public static LedgerDataStructure deserialize(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return (LedgerDataStructure) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
