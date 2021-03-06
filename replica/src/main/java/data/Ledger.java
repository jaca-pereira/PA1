package data;


import redis.clients.jedis.Jedis;
import com.google.gson.Gson;

import java.util.*;

public class Ledger {
    
    private Jedis jedis;

    public void toJedis(LedgerDataStructure ledger) {
        Gson gson = new Gson();
        String json = gson.toJson(ledger);
        jedis.set("ledger",json);
    }

    public LedgerDataStructure fromJedis() {
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
        System.out.println("foi para o redis");
        return account;
    }

    public int getBalance(byte[] account) {
       return this.fromJedis().getBalance(account);
    }

    public int getGlobalValue() {
        return this.fromJedis().getGlobalValue();
    }

    public List<Transaction> getExtract(byte[] account) {
        return this.fromJedis().getExtract(account);
    }

    public int getTotalValue(List<byte[]> accounts) {
       return this.fromJedis().getTotalValue(accounts);
    }

    public void sendTransaction(Transaction t) {
        LedgerDataStructure ledger = this.fromJedis();
        ledger.transaction(t);
        this.toJedis(ledger);
        System.out.println("foi para o redis");
    }

    public List<Block> getLedger() {
        return this.fromJedis().getLedger();
    }


    public void addMinedBlock(Block block) throws Exception {
        LedgerDataStructure ledger = this.fromJedis();
        ledger.addMinedBlock(block);
        this.toJedis(ledger);
        System.out.println("foi para o redis");
    }

    public Block getBlockToMine() throws Exception {
        LedgerDataStructure ledger = this.fromJedis();
        Block blockToMine = ledger.getBlockToMine();
        this.toJedis(ledger);
        System.out.println("foi para o redis");
        return blockToMine;
    }

}
