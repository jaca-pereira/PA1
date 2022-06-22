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

import java.util.logging.Level;

import java.util.logging.Logger;

public class LedgerReplica extends DefaultSingleRecoverable {
    public static final byte[] LEDGER = new byte[]{0x0};
    public static final int PORT = 6379;
    private Ledger ledger;
    private Logger logger;
    private ServiceReplica serviceReplica;

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
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "172.18.0.3" + id, PORT);
        return jedisPool.getResource();
    }

    public LedgerReplica(int id) throws IOException {
        ledger = new Ledger(this.initRedis(id));
        logger = Logger.getLogger(LedgerReplica.class.getName());
        BasicConfigurator.configure();
        serviceReplica = new ServiceReplica(id, this, this);
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
            Reply rep;
            if(!Security.verifySignature(request.getPublicKey(), request.getRequestType().toString().getBytes(), request.getSignature()))
                throw new IllegalArgumentException("Signature not valid!");
            switch (request.getRequestType()) {
                case CREATE_ACCOUNT:
                    rep= new Reply(this.ledger.addAccount(request.getAccount()),  LedgerRequestType.CREATE_ACCOUNT);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.CREATE_ACCOUNT.toString().getBytes()));
                    objOut.writeObject(rep);
                    break;
                case LOAD_MONEY:
                    rep = new Reply(this.loadMoney(request), LedgerRequestType.LOAD_MONEY);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.LOAD_MONEY.toString().getBytes()));
                    objOut.writeObject(rep);
                    break;
                case SEND_TRANSACTION:
                    rep =new Reply(this.sendTransaction(request), LedgerRequestType.SEND_TRANSACTION);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.SEND_TRANSACTION.toString().getBytes()));
                    objOut.writeObject(rep);
                    break;
                case GET_BALANCE:
                    rep = new Reply(this.ledger.getBalance(request.getAccount()), LedgerRequestType.GET_BALANCE);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.GET_BALANCE.toString().getBytes()));
                    objOut.writeObject(rep);
                    break;
                case GET_EXTRACT:
                    rep = new Reply(this.ledger.getExtract(request.getAccount()), LedgerRequestType.GET_EXTRACT);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.GET_EXTRACT.toString().getBytes()));
                    objOut.writeObject(rep);
                    break;
                case GET_TOTAL_VALUE:
                    rep = new Reply(this.ledger.getTotalValue(request.getAccounts()), LedgerRequestType.GET_TOTAL_VALUE);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.GET_TOTAL_VALUE.toString().getBytes()));
                    objOut.writeObject(rep);
                    break;
                case GET_GLOBAL_VALUE:
                    rep = new Reply(this.ledger.getGlobalValue(), LedgerRequestType.GET_GLOBAL_VALUE);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.GET_GLOBAL_VALUE.toString().getBytes()));
                    objOut.writeObject(rep);
                    break;
                case GET_LEDGER:
                    rep = new Reply(this.ledger.getLedger(), LedgerRequestType.GET_LEDGER);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.GET_LEDGER.toString().getBytes()));
                    objOut.writeObject(rep);
                    break;
                case GET_BLOCK_TO_MINE:
                    rep = new Reply(this.ledger.getBlockToMine(), LedgerRequestType.GET_BLOCK_TO_MINE);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.GET_BLOCK_TO_MINE.toString().getBytes()));
                    objOut.writeObject(rep);
                    break;
                case MINE_BLOCK:
                    rep = new Reply(this.ledger.addMinedBlock(request.getBlock()), LedgerRequestType.MINE_BLOCK);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.MINE_BLOCK.toString().getBytes()));
                    objOut.writeObject(rep);
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
            Reply rep;
            switch (request.getRequestType()) {
                case GET_BALANCE:
                    rep = new Reply(this.ledger.getBalance(request.getAccount()), LedgerRequestType.GET_BALANCE);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.GET_BALANCE.toString().getBytes()));
                    objOut.writeObject(rep);
                    break;
                case GET_EXTRACT:
                    rep = new Reply(this.ledger.getExtract(request.getAccount()), LedgerRequestType.GET_EXTRACT);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.GET_EXTRACT.toString().getBytes()));
                    objOut.writeObject(rep);
                    break;
                case GET_TOTAL_VALUE:
                    rep = new Reply(this.ledger.getTotalValue(request.getAccounts()), LedgerRequestType.GET_TOTAL_VALUE);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.GET_TOTAL_VALUE.toString().getBytes()));
                    objOut.writeObject(rep);
                    break;
                case GET_GLOBAL_VALUE:
                    rep = new Reply(this.ledger.getGlobalValue(), LedgerRequestType.GET_GLOBAL_VALUE);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.GET_GLOBAL_VALUE.toString().getBytes()));
                    objOut.writeObject(rep);
                    break;
                case GET_LEDGER:
                    rep = new Reply(this.ledger.getLedger(), LedgerRequestType.GET_LEDGER);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.GET_LEDGER.toString().getBytes()));
                    objOut.writeObject(rep);
                    break;
                case GET_BLOCK_TO_MINE:
                    rep = new Reply(this.ledger.getBlockToMine(), LedgerRequestType.GET_BLOCK_TO_MINE);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.GET_BLOCK_TO_MINE.toString().getBytes()));
                    objOut.writeObject(rep);
                    break;
                case MINE_BLOCK:
                    rep = new Reply(this.ledger.addMinedBlock(request.getBlock()), LedgerRequestType.MINE_BLOCK);
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.MINE_BLOCK.toString().getBytes()));
                    objOut.writeObject(rep);
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
