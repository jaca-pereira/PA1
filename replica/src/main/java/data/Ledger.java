package data;


import redis.clients.jedis.Jedis;
import com.google.gson.Gson;

import java.util.*;

public class Ledger {

    public static final byte[] LEDGER = new byte[]{0x0};
    private int operationsCounter;
    private int globalValue;

    private Jedis jedis;
    private LedgerDataStructure ledger;

    private void serializeToJedis() {
        Gson gson = new Gson();
        String json = gson.toJson(this.ledger.getLedger());
        jedis.set("ledger",json);
    }

    private void incrementCounter() {
        this.operationsCounter++;
        if (this.operationsCounter == 4) {
            this.operationsCounter = 0;
            this.serializeToJedis();
        }
    }

    public Ledger(Jedis jedis) {
        this.ledger = new LedgerDataStructure();
        this.operationsCounter = 0;
        this.globalValue = 0;
        this.jedis = jedis;
        this.serializeToJedis();
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
