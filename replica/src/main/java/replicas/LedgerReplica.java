package replicas;


import Security.Security;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import data.*;
import org.apache.log4j.BasicConfigurator;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.util.List;
import java.util.logging.Level;

import java.util.logging.Logger;

public class LedgerReplica extends DefaultSingleRecoverable {
    public static final byte[] LEDGER = new byte[]{0x0};
    private Ledger ledger;
    private Logger logger;

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: LedgerReplica <id>");
            System.exit(-1);
        }
        new LedgerReplica(Integer.parseInt(args[0]));
    }

    private Jedis initRedis(int id) throws IOException {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(128);
        jedisPoolConfig.setMaxIdle(128);
        jedisPoolConfig.setMinIdle(120);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "172.18.0.2" + id, 6379);
        return jedisPool.getResource();
    }

    public LedgerReplica(int id) throws IOException {
        ledger = new Ledger(this.initRedis(id));
        logger = Logger.getLogger(LedgerReplica.class.getName());
        BasicConfigurator.configure();
        new ServiceReplica(id, this, this);
    }
    private boolean loadMoney(Request request) {
        if (request.getValue() < 0)
            throw new IllegalArgumentException("Value must be positive!");
        this.ledger.sendTransaction(new Transaction(this.LEDGER, request.getAccount(), -1, request.getValue(), request.getSignature()));
        return true;
    }

    private boolean sendTransaction(Request request) {
        if (request.getValue() < 0)
            throw new IllegalArgumentException("Value must be positive!");
        this.ledger.sendTransaction(new Transaction(request.getAccount(), request.getAccountDestiny(), request.getNonce(), request.getValue(), request.getSignature()));
        return true;
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        byte[] reply = null;
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
            ObjectInput objIn = new ObjectInputStream(byteIn);
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
            Request request = (Request) objIn.readObject();
            if(!Security.verifySignature(request.getPublicKey(), request.getRequestType().toString().getBytes(), request.getSignature()))
                throw new IllegalArgumentException("Signature not valid!");
            switch (request.getRequestType()) {
                case CREATE_ACCOUNT:
                    objOut.writeObject(new Reply(this.ledger.addAccount(request.getAccount())));
                    break;
                case LOAD_MONEY:
                    objOut.writeObject(new Reply(this.loadMoney(request)));
                    break;
                case SEND_TRANSACTION:
                    objOut.writeObject(new Reply(this.sendTransaction(request)));
                    break;
                case GET_BALANCE:
                    objOut.writeObject(new Reply(this.ledger.getBalance(request.getAccount())));
                    break;
                case GET_EXTRACT:
                    objOut.writeObject(new Reply(this.ledger.getExtract(request.getAccount())));
                    break;
                case GET_TOTAL_VALUE:
                    objOut.writeObject(new Reply(this.ledger.getTotalValue(request.getAccounts())));
                    break;
                case GET_GLOBAL_VALUE:
                    objOut.writeObject(new Reply(this.ledger.getGlobalValue()));
                    break;
                case GET_LEDGER:
                    objOut.writeObject(new Reply(this.ledger.getLedger()));
                    break;
                case GET_BLOCK_TO_MINE:
                    objOut.writeObject(new Reply(this.ledger.getBlockToMine()));
                    break;
                case MINE_BLOCK:
                    objOut.writeObject(new Reply(this.ledger.addMineratedBlock(request.getBlock())));
                    break;
                default:
                    throw new UnsupportedOperationException("Operation does not exist!");
            }
            objOut.flush();
            byteOut.flush();
            reply = byteOut.toByteArray();
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Ocurred during operation execution", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }



    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        byte[] reply = null;
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
            Request request = (Request) objIn.readObject();
            switch (request.getRequestType()) {
                case GET_BALANCE:
                    objOut.writeObject(new Reply(this.ledger.getBalance(request.getAccount())));
                    break;
                case GET_EXTRACT:
                    objOut.writeObject(new Reply(this.ledger.getExtract(request.getAccount())));
                    break;
                case GET_TOTAL_VALUE:
                    objOut.writeObject(new Reply(this.ledger.getTotalValue(request.getAccounts())));
                    break;
                case GET_GLOBAL_VALUE:
                    objOut.writeObject(new Reply(this.ledger.getGlobalValue()));
                    break;
                case GET_LEDGER:
                    objOut.writeObject(new Reply(this.ledger.getLedger()));
                    break;
                case GET_BLOCK_TO_MINE:
                    objOut.writeObject(new Reply(this.ledger.getBlockToMine()));
                    break;
                default:
                    throw new UnsupportedOperationException("Operation does not exist!");
            }
            objOut.flush();
            byteOut.flush();
            reply = byteOut.toByteArray();
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Ocurred during operation execution", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    @Override
    public byte[] getSnapshot() {

        return new byte[0];
    }



    @Override
    public void installSnapshot(byte[] state) {

    }
}
