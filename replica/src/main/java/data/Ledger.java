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

    public Ledger(Jedis jedis, int difficulty) {
        LedgerDataStructure ledger = new LedgerDataStructure(difficulty);
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

    public List<Transaction> getExtract(byte[] account, int begin) {
        return this.fromJedis().getExtract(account, begin);
    }

    public int getTotalValue(List<byte[]> accounts) {
       return this.fromJedis().getTotalValue(accounts);
    }

    public void sendTransaction(Transaction t) {
        LedgerDataStructure ledger = this.fromJedis();
        ledger.transaction(t);
        this.toJedis(ledger);
    }

    public LedgerDataStructure getLedger() {
        return this.fromJedis();
    }


    public void addMinedBlock(Block block) throws Exception {
        LedgerDataStructure ledger = this.fromJedis();
        ledger.addMinedBlock(block);
        this.toJedis(ledger);
    }

    public Block getBlockToMine() throws Exception {
        return this.fromJedis().getBlockToMine();
    }

}
