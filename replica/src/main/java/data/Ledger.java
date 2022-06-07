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
       return this.fromJedis().getBalance(account);
    }

    public int getGlobalValue() {
        return this.fromJedis().getGlobalValue();
    }

    public List<Transaction> getExtract(byte[] account) {
        //TODO
        return null;
    }

    public int getTotalValue(List<byte[]> accounts) {
       return this.fromJedis().getTotalValue(accounts);
    }

    public void sendTransaction(Transaction t) {
        LedgerDataStructure ledger = this.fromJedis();
        ledger.transaction(t);
        this.toJedis(ledger);
    }

    public List<Transaction> getLedger() {
        //TODO
        return null;
    }

    public List<Transaction> getTransactionsToMinerate() {
        return this.fromJedis().getTransactionsToMinerate();
    }

    public boolean addMineratedBlock(Block block) {
        LedgerDataStructure ledger = this.fromJedis();
        boolean didMineration = ledger.addMineratedBlock(block);
        this.toJedis(ledger);
        return didMineration;
    }
}
