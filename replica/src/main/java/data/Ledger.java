package data;


import redis.clients.jedis.Jedis;
import com.google.gson.Gson;

import java.util.*;

public class Ledger {
    
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

    public int getBalance(byte[] account) {
        LedgerDataStructure ledger = fromJedis();
        int balance = ledger.getBalance(account);
        this.toJedis(ledger);
        return balance;
    }

    public int getGlobalValue() {
        return this.fromJedis().getGlobalValue();
    }

    public List<Transaction> getExtract(byte[] account) {
        LedgerDataStructure ledger = fromJedis();
        List<Transaction> extract = ledger.getExtract(account);
        this.toJedis(ledger);
        return extract;
    }

    public int getTotalValue(List<byte[]> accounts) {
        LedgerDataStructure ledger = fromJedis();
        int totalValue = ledger.getTotalValue(accounts);
        this.toJedis(ledger);
        return totalValue;
    }

    public void sendTransaction(Transaction t) {
        LedgerDataStructure ledger = fromJedis();
        ledger.transaction(t);
        this.toJedis(ledger);
    }

    public List<Transaction> getLedger() {
        return fromJedis().getLedger();
    }

}
