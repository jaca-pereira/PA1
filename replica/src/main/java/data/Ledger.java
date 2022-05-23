package data;


import redis.clients.jedis.Jedis;
import com.google.gson.Gson;

import java.util.*;

public class Ledger {

    public static final byte[] LEDGER = new byte[]{0x0};
    private Jedis jedis;

    private void toJedis(LedgerDataStructure ledger) {
        Gson gson = new Gson();
        String json = gson.toJson(ledger);
        jedis.set("ledger",json);
    }

    private LedgerDataStructure fromJedis() {
        String json = jedis.get("ledger");
        Gson gson = new Gson();
        return gson.fromJson(json, LedgerDataStructure.class);
    }

    public Ledger(Jedis jedis) {
        LedgerDataStructure ledger = new LedgerDataStructure();
        this.jedis = jedis;
        this.toJedis(ledger);
    }

    public byte[]  addAccount(byte[] account) {
        LedgerDataStructure ledger= this.fromJedis();
        ledger.addAccount(account);
        this.toJedis(ledger);
        return account;
    }

    public int getBalance(Transaction t) {
        LedgerDataStructure ledger = fromJedis();
        Account account = ledger.transaction(t);
        this.toJedis(ledger);
        return account.getBalance();
    }

    public int getGlobalValue() {
        return this.fromJedis().getGlobalValue();
    }

    public List<Transaction> getExtract(Transaction t) {
        LedgerDataStructure ledger = fromJedis();
        Account account = ledger.transaction(t);
        this.toJedis(ledger);
        return account.getTransactionList();
    }

    public int getTotalValue(Transaction t) {
        LedgerDataStructure ledger = fromJedis();
        int totalValue = ledger.transactionMultipleAccounts(t);
        this.toJedis(ledger);
        return totalValue;
    }

    public void sendTransaction(Transaction t) {
        LedgerDataStructure ledger = fromJedis();
        ledger.transactionBetweenAccounts(t);
        this.toJedis(ledger);
    }

    public List<Transaction> getLedger() {
        return fromJedis().getLedger();
    }

}
