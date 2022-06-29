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

public class LedgerReplica extends DefaultSingleRecoverable {

    public static final int PORT = 6379;
    private static final byte[] LEDGER = new byte[]{0x0};
    private static final int FIRST_X_BLOCKS = 4;
    private final boolean async;
    private Ledger ledger;
    private ServiceReplica serviceReplica;
    private int reward;
    private int rewardCounter;

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: LedgerReplica <id> <async>");
            System.exit(-1);
        }
        new LedgerReplica(Integer.parseInt(args[0]), Boolean.valueOf(args[1]));
    }

    private Jedis initRedis(int id) throws IOException {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(128);
        jedisPoolConfig.setMaxIdle(128);
        jedisPoolConfig.setMinIdle(120);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "172.19.30." + id, PORT); //id-20 para que o replica id não se misture com o proxy id
        return jedisPool.getResource();
    }

    public LedgerReplica(int id, boolean async) throws IOException {
        ledger = new Ledger(this.initRedis(id));
        BasicConfigurator.configure();
        serviceReplica = new ServiceReplica(id, this, this);
        this.reward = 100;
        this.rewardCounter = 0;
        this.async = async;
    }

    private boolean sendTransaction(Request request) {
        if (request.getValue() < 0)
            throw new IllegalArgumentException("Value must be positive!");
        this.ledger.sendTransaction(new Transaction(request.getAccount(), request.getAccountDestiny(), request.getNonce(), request.getValue(), request.getSignature()));
        return true;
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        byte[] reply;
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
            ObjectInput objIn = new ObjectInputStream(byteIn);
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
                Request request = (Request) objIn.readObject();
                Reply rep;
                if(Security.verifySignature(request.getPublicKey(), request.getRequestType().toString().getBytes(), request.getSignature())) {
                    switch (request.getRequestType()) {
                        case CREATE_ACCOUNT:
                            rep = new Reply(this.ledger.addAccount(request.getAccount()), LedgerRequestType.CREATE_ACCOUNT);
                            break;
                        case SEND_TRANSACTION:
                            rep = new Reply(this.sendTransaction(request), LedgerRequestType.SEND_TRANSACTION);
                            break;
                        case GET_BALANCE:
                            rep = new Reply(this.ledger.getBalance(request.getAccount()), LedgerRequestType.GET_BALANCE);
                            break;
                        case GET_EXTRACT:
                            rep = new Reply(this.ledger.getExtract(request.getAccount(), request.getValue()), LedgerRequestType.GET_EXTRACT);
                            break;
                        case GET_TOTAL_VALUE:
                            rep = new Reply(this.ledger.getTotalValue(request.getAccounts()), LedgerRequestType.GET_TOTAL_VALUE);
                            break;
                        case GET_GLOBAL_VALUE:
                            rep = new Reply(this.ledger.getGlobalValue(), LedgerRequestType.GET_GLOBAL_VALUE);
                            break;
                        case GET_LEDGER:
                            rep = new Reply(this.ledger.getLedger(), LedgerRequestType.GET_LEDGER);
                            break;
                        case GET_BLOCK_TO_MINE:
                            rep = new Reply(this.ledger.getBlockToMine(), LedgerRequestType.GET_BLOCK_TO_MINE);
                            break;
                        case MINE_BLOCK:
                            this.ledger.addMinedBlock(request.getBlock());
                            System.out.println("MINOU");
                            Request rewardRequest = new Request(LedgerRequestType.LOAD_MONEY, this.LEDGER, request.getAccount(), this.reward, -1);
                            rewardRequest.setPublicKey(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                            rewardRequest.setSignature(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.LOAD_MONEY.toString().getBytes()));
                            this.sendTransaction(rewardRequest);
                            System.out.println("ENVIOU TRANSACTION");
                            if(++rewardCounter == FIRST_X_BLOCKS)
                                this.reward = 10;
                            rep = new Reply(reward, LedgerRequestType.MINE_BLOCK);
                            break;
                        default:
                            rep = new Reply("Operation not supported");
                    }
                } else {
                    System.out.println("MAL ASSINADO");
                    rep = new Reply("Bad signature");
                }
                if (async) {
                    rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), request.getRequestType().toString().getBytes()));
                }
                objOut.writeObject(rep);
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
        } catch (Exception e) {
            try {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutput objOut = new ObjectOutputStream(byteOut);
                e.printStackTrace();
                Reply repError = new Reply(e.getMessage());
                if (async) {
                    repError.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    repError.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.ERROR.toString().getBytes()));
                }
                objOut.writeObject(repError);
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            } catch (IOException ex) {
                e.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
        System.out.println("VAI ENVIAR RESPOSTA");
        return reply;
    }



    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        byte[] reply;
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
            Request request = (Request) objIn.readObject();
            Reply rep;
            if(Security.verifySignature(request.getPublicKey(), request.getRequestType().toString().getBytes(), request.getSignature())) {
                switch (request.getRequestType()) {
                    case GET_BALANCE:
                        rep = new Reply(this.ledger.getBalance(request.getAccount()), LedgerRequestType.GET_BALANCE);
                        break;
                    case GET_EXTRACT:
                        rep = new Reply(this.ledger.getExtract(request.getAccount(), request.getValue()), LedgerRequestType.GET_EXTRACT);
                        break;
                    case GET_TOTAL_VALUE:
                        rep = new Reply(this.ledger.getTotalValue(request.getAccounts()), LedgerRequestType.GET_TOTAL_VALUE);
                        break;
                    case GET_GLOBAL_VALUE:
                        rep = new Reply(this.ledger.getGlobalValue(), LedgerRequestType.GET_GLOBAL_VALUE);
                        break;
                    case GET_LEDGER:
                        rep = new Reply(this.ledger.getLedger(), LedgerRequestType.GET_LEDGER);
                        break;
                    case GET_BLOCK_TO_MINE:
                        rep = new Reply(this.ledger.getBlockToMine(), LedgerRequestType.GET_BLOCK_TO_MINE);
                        break;
                    default:
                        rep = new Reply("Operation not supported");
                }
            } else {
                System.out.println("MAL ASSINADO");
                rep = new Reply("Bad signature");
            }
            if (async) {
                rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), request.getRequestType().toString().getBytes()));
            }
            objOut.writeObject(rep);
            objOut.flush();
            byteOut.flush();
            reply = byteOut.toByteArray();
        } catch (Exception e) {
            try {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutput objOut = new ObjectOutputStream(byteOut);
                e.printStackTrace();
                Reply repError = new Reply(e.getMessage());
                if (async) {
                    repError.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                    repError.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.ERROR.toString().getBytes()));
                }
                objOut.writeObject(repError);
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            } catch (IOException ex) {
                e.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
        System.out.println("VAI ENVIAR RESPOSTA");
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
