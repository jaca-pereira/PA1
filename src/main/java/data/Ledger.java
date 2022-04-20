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
        this.ledger = new TreeMap<>();
        this.operationsCounter = 0;
        this.globalValue = 0;
    }

    public byte[]  addAccount(byte[] account) {
        if (this.ledger.get(account)!=null)
            throw new IllegalArgumentException("Account already exists!");
        this.ledger.put(account.toString(), new LinkedList<Transaction>());
        this.incrementCounter();
        return account;
    }

    public int getBalance(byte[] account) {
        List<Transaction> transactionsList = this.ledger.get(account.toString());
        if (transactionsList == null)
            throw new IllegalArgumentException("Account does not exist!");
        int balance = 0;
        for(Transaction t: transactionsList) {
            if (Arrays.equals(t.getOriginalAccount(), account))
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
        List<Transaction> transactionsList = this.ledger.get(account.toString());
        if (transactionsList == null)
            throw new IllegalArgumentException("Account does not exist!");
        this.incrementCounter();
        return transactionsList;
    }


    //alterar daqui para baixo
    public int getTotalValue(List<byte[]> accounts) {

        //implementar mapa com transações list e balance como values
        int total = 0;
        for (byte[] i: accounts) {
            if (this.ledger.get(i.toString())==null)
                throw new IllegalArgumentException("Account " + i + " does not exist!");
            total += this.getBalance(i);
        }
        return total;
    }

    public void sendTransaction(byte[] originAccount, byte[] destinationAccount, int value, long nonce) {
        List<Transaction> destinationTransactionsList = this.ledger.get(destinationAccount.toString());
        if (destinationTransactionsList == null)
            throw new IllegalArgumentException("Destination account does not exist!");
        if (!Arrays.equals(originAccount, LEDGER)) {
            List<Transaction> originTransactionsList = this.ledger.get(originAccount.toString());
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
