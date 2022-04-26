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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;

import java.util.logging.Logger;

public class LedgerReplica extends DefaultSingleRecoverable {

    public static final int PORT = 6379;
    private Ledger ledger;
    private Logger logger;

    public static void main(String[] args) throws UnknownHostException {
        if (args.length < 1) {
            System.out.println("Usage: LedgerReplica <id>");
            System.exit(-1);
        }
        new LedgerReplica(Integer.parseInt(args[0]));
    }

    public LedgerReplica(int id) {
        String ip = "172.18.0.21";
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(128);
        jedisPoolConfig.setMaxIdle(128);
        jedisPoolConfig.setMinIdle(100);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, ip, PORT);
        Jedis jedis = jedisPool.getResource();
        ledger = new Ledger(jedis);
        logger = Logger.getLogger(LedgerReplica.class.getName());
        BasicConfigurator.configure();
        new ServiceReplica(id, this, this);
    }

    private byte[]  createAccount(Request request) {
        if(!Security.verifySignature(request.getPublicKey(), request.getRequestType().toString().getBytes(), request.getSignature()))
            throw new IllegalArgumentException("Signature not valid!");
        return this.ledger.addAccount(request.getAccount());
    }

    private boolean loadMoney(Request request) {
        if(!Security.verifySignature(request.getPublicKey(),request.getRequestType().toString().getBytes(), request.getSignature()))
            throw new IllegalArgumentException("Signature not valid!");
        if (request.getValue() < 0)
            throw new IllegalArgumentException("Value must be positive!");
        this.ledger.sendTransaction(new Transaction(request.getRequestType(), Ledger.LEDGER, request.getAccount(), request.getValue(), -1));
        return true;
    }

    private int getBalance(Request request) {
        if(!Security.verifySignature(request.getPublicKey(),request.getRequestType().toString().getBytes(), request.getSignature()))
            throw new IllegalArgumentException("Signature not valid!");
        return this.ledger.getBalance(new Transaction(request.getRequestType(), request.getAccount()));
    }

    private List<Transaction> getExtract(Request request) {
        if(!Security.verifySignature(request.getPublicKey(),request.getRequestType().toString().getBytes(), request.getSignature()))
            throw new IllegalArgumentException("Signature not valid!");
        return this.ledger.getExtract(new Transaction(request.getRequestType(), request.getAccount()));
    }

    private boolean sendTransaction(Request request) {
        if(!Security.verifySignature(request.getPublicKey(),request.getRequestType().toString().getBytes(), request.getSignature()))
            throw new IllegalArgumentException("Signature not valid!");
        if (request.getValue() < 0)
            throw new IllegalArgumentException("Value must be positive!");
        this.ledger.sendTransaction(new Transaction(request.getRequestType(), request.getAccount(), request.getAccountDestiny(), request.getValue(), request.getNonce()));
        return true;
    }

    private int getTotalValue(Request request) {
        if(!Security.verifySignature(request.getPublicKey(),request.getRequestType().toString().getBytes(), request.getSignature()))
            throw new IllegalArgumentException("Signature not valid!");
        return this.ledger.getTotalValue(new Transaction(request.getRequestType(), request.getAccounts()));
    }

    private int getGlobalValue(Request request) {
        if(!Security.verifySignature(request.getPublicKey(),request.getRequestType().toString().getBytes(), request.getSignature()))
            throw new IllegalArgumentException("Signature not valid!");
        return this.ledger.getGlobalValue();
    }

    private List<Transaction> getLedger() {
        return this.ledger.getLedger();
    }

    // bft

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        byte[] reply = null;
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
            Request request = (Request) objIn.readObject();
            switch (request.getRequestType()) {
                case CREATE_ACCOUNT:
                    objOut.writeObject(new Reply(this.createAccount(request)));
                    break;
                case LOAD_MONEY:
                    objOut.writeObject(new Reply(this.loadMoney(request)));
                    break;
                case SEND_TRANSACTION:
                    objOut.writeObject(new Reply(this.sendTransaction(request)));
                    break;
                case GET_BALANCE:
                    objOut.writeObject(new Reply(this.getBalance(request)));
                    break;
                case GET_EXTRACT:
                    objOut.writeObject(new Reply(this.getExtract(request)));
                    break;
                case GET_TOTAL_VALUE:
                    objOut.writeObject(new Reply(this.getTotalValue(request)));
                    break;
                case GET_GLOBAL_VALUE:
                    objOut.writeObject(new Reply(this.getGlobalValue(request)));
                    break;
                case GET_LEDGER:
                    objOut.writeObject(new Reply(this.getLedger()));
                    break;
                default:
                    throw new UnsupportedOperationException("Operation does not exist!");
            }
            objOut.flush();
            byteOut.flush();
            reply = byteOut.toByteArray();
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Ocurred during operation execution", e);
        }
        return reply;
    }

    @SuppressWarnings("unchecked")
    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        byte[] reply = null;
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
            Request request = (Request) objIn.readObject();
            switch (request.getRequestType()) {
                case GET_BALANCE:
                    objOut.writeObject(new Reply(this.getBalance(request)));
                    break;
                case GET_EXTRACT:
                    objOut.writeObject(new Reply(this.getExtract(request)));
                    break;
                case GET_TOTAL_VALUE:
                    objOut.writeObject(new Reply(this.getTotalValue(request)));
                    break;
                case GET_GLOBAL_VALUE:
                    objOut.writeObject(new Reply(this.getGlobalValue(request)));
                    break;
                case GET_LEDGER:
                    objOut.writeObject(new Reply(this.getLedger()));
                    break;
                default:
                    throw new UnsupportedOperationException("Operation does not exist!");
            }
            objOut.flush();
            byteOut.flush();
            reply = byteOut.toByteArray();
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Ocurred during operation execution", e);
        }
        return reply;
    }

    @Override
    public byte[] getSnapshot() {
        /*try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
            objOut.writeObject(ledger);
            return byteOut.toByteArray();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while taking snapshot", e);
        }*/
        return new byte[0];
    }


    @SuppressWarnings("unchecked")
    @Override
    public void installSnapshot(byte[] state) {
        /*try (ByteArrayInputStream byteIn = new ByteArrayInputStream(state);
             ObjectInput objIn = new ObjectInputStream(byteIn)) {
            ledger = (Ledger) objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error while installing snapshot", e);
        }*/
    }
}
