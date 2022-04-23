package data;


import java.util.*;

public class Ledger {

    public static final byte[] LEDGER = new byte[]{0x0};
    private int operationsCounter;
    private int globalValue;

    private LedgerDataStructure ledger;

    //mais simples ter uma lista com as operações ordenadas
    //um inteiro a representar o balance da conta tb
    //fazer uma classe

    private void incrementCounter() {
        this.operationsCounter++;
        if (this.operationsCounter == 20) {
            //serializar :)
            this.operationsCounter = 0;
        }
    }

    public Ledger() {
        this.ledger = new LedgerDataStructure();
        this.operationsCounter = 0;
        this.globalValue = 0;
    }

    public byte[]  addAccount(byte[] account) {
        this.ledger.addAccount(account);
        this.incrementCounter();
        return account;
    }

    public int getBalance(Transaction t) {
        Account account = this.ledger.transaction(t);
        this.incrementCounter();
        return account.getBalance();
    }

    public int getGlobalValue() {
        this.incrementCounter();
        return this.globalValue;
    }

    public List<Transaction> getExtract(Transaction t) {
        Account account = this.ledger.transaction(t);
        this.incrementCounter();
        return account.getTransactionList();
    }

    public int getTotalValue(Transaction t) {
        this.incrementCounter();
        return this.ledger.transactionMultipleAccounts(t);
    }

    public void sendTransaction(Transaction t) {
        this.incrementCounter();
        this.ledger.transactionBetweenAccounts(t);
        if(t.getNonce() == -1)
            this.globalValue += t.getValue();
    }

    public List<Transaction> getLedger() {
        this.incrementCounter();
        return this.ledger.getLedger();
    }

}
