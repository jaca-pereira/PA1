package data;


import redis.clients.jedis.Jedis;
import com.google.gson.Gson;

import java.util.*;

public class Ledger {

    public static final byte[] LEDGER = new byte[]{0x0};
    private Jedis jedis;
    private LedgerDataStructure ledger;

    private void serializeToJedis() {
        Gson gson = new Gson();
        String json = gson.toJson(this.ledger.getLedger());
        jedis.set("ledger",json);
    }

    public Ledger(Jedis jedis) {
        this.ledger = new LedgerDataStructure();
        this.jedis = jedis;
        this.serializeToJedis();
    }

    public byte[]  addAccount(byte[] account) {
        this.ledger.addAccount(account);
        this.serializeToJedis();
        return account;
    }

    public int getBalance(Transaction t) {
        Account account = this.ledger.transaction(t);
        this.serializeToJedis();
        return account.getBalance();
    }

    public int getGlobalValue() {
        this.serializeToJedis();
        return this.ledger.getGlobalValue();
    }

    public List<Transaction> getExtract(Transaction t) {
        Account account = this.ledger.transaction(t);
        this.serializeToJedis();
        return account.getTransactionList();
    }

    public int getTotalValue(Transaction t) {
        this.serializeToJedis();
        return this.ledger.transactionMultipleAccounts(t);
    }

    public void sendTransaction(Transaction t) {
        this.serializeToJedis();
        this.ledger.transactionBetweenAccounts(t);
    }

    public List<Transaction> getLedger() {
        this.serializeToJedis();
        return this.ledger.getLedger();
    }

}
