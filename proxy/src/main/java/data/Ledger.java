package data;


import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

public class Ledger {

    private static final int PORT = 6379;
    private Jedis jedis;

    public void toJedis(LedgerDataStructure ledger) {
        Gson gson = new Gson();
        String json = gson.toJson(ledger);
        System.out.println(json);
        jedis.set("ledger",json);
    }

    public LedgerDataStructure fromJedis() {
        String json = jedis.get("ledger");
        Gson gson = new Gson();
        return gson.fromJson(json, LedgerDataStructure.class);
    }

    private Jedis initRedis()  {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(128);
        jedisPoolConfig.setMaxIdle(128);
        jedisPoolConfig.setMinIdle(120);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "127.19.20.0", PORT);
        return jedisPool.getResource();
    }

    public Ledger() {
        LedgerDataStructure ledger = new LedgerDataStructure();
        this.jedis = this.initRedis();
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
        return this.fromJedis().getExtract(account);
    }

    public int getTotalValue(List<byte[]> accounts) {
       return this.fromJedis().getTotalValue(accounts);
    }

    public void sendTransaction(Transaction t) {
        LedgerDataStructure ledger = this.fromJedis();
        ledger.transaction(t);
        this.toJedis(ledger);
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
