package data;


import java.io.Serializable;
import java.util.*;

public class Ledger {

    public static final byte[] LEDGER = new byte[]{0x0};
    private int operationsCounter;
    private int globalValue;
    private Map<String, List<Transaction>> ledger;

    private void incrementCounter() {
        this.operationsCounter++;
        if (this.operationsCounter == 20) {
            //serializar :)
        }
    }

    public Ledger() {
        this.ledger = new HashMap<>();
        this.operationsCounter = 0;
        this.globalValue = 0;
    }

    public byte[]  addAccount(byte[] account) {
        if (this.ledger.get(new String(account))!=null)
            throw new IllegalArgumentException("Account already exists!");
        this.ledger.put(new String(account), new LinkedList<>());
        this.incrementCounter();
        return account;
    }

    public int getBalance(byte[] account) {
        List<Transaction> transactionsList = this.ledger.get(new String(account));
        if (transactionsList == null)
            throw new IllegalArgumentException("Account does not exist!");
        int balance = 0;
        for(Transaction t: transactionsList) {
            if (new String(t.getOriginalAccount()).equals(new String(account)))
                balance -= t.getValue();
            else
                balance += t.getValue();
        }
        this.incrementCounter();
        return balance;
    }

    public int getGlobalValue() {
        return this.globalValue;
    }

    public List<Transaction> getExtract(byte[] account) {
        List<Transaction> transactionsList = this.ledger.get(new String(account));
        if (transactionsList == null)
            throw new IllegalArgumentException("Account does not exist!");
        this.incrementCounter();
        return transactionsList;
    }



    public int getTotalValue(List<byte[]> accounts) {


        int total = 0;
        for (byte[] i: accounts) {
            if (this.ledger.get(new String(i))==null)
                throw new IllegalArgumentException("Account " + new String(i) + " does not exist!");
            total += this.getBalance(i);
        }
        return total;
    }

    public void sendTransaction(byte[] originAccount, byte[] destinationAccount, int value, long nonce) {
        List<Transaction> destinationTransactionsList = this.ledger.get(new String(destinationAccount));
        if (destinationTransactionsList == null)
            throw new IllegalArgumentException("Destination account does not exist!");
        if (!Arrays.equals(originAccount, LEDGER)) {
            List<Transaction> originTransactionsList = this.ledger.get(new String(originAccount));
            if (originTransactionsList == null)
                throw new IllegalArgumentException("Origin account does not exist!");
            originTransactionsList.add(new Transaction(originAccount, destinationAccount, value, nonce));
        } else {
            this.globalValue += value;
        }
        destinationTransactionsList.add(new Transaction(originAccount, destinationAccount, value, nonce));
        this.incrementCounter();
    }

    public Map<String, List<Transaction>> getLedger() {
        return this.ledger;
    }
}
