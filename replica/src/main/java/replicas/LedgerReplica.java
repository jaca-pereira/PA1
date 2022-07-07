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
    private Ledger ledger;
    private ServiceReplica serviceReplica;
    public static final int REWARD = 30;

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: LedgerReplica <id>");
            System.exit(-1);
        }
        new LedgerReplica(Integer.parseInt(args[0]));
    }

    private Jedis initRedis(int id)  {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(128);
        jedisPoolConfig.setMaxIdle(128);
        jedisPoolConfig.setMinIdle(120);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "redis_" + id, PORT);
        return jedisPool.getResource();
    }

    public LedgerReplica(int id) {
        ledger = new Ledger(this.initRedis(id));
        BasicConfigurator.configure();
        serviceReplica = new ServiceReplica(id, this, this);
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
                            rep = new Reply(this.ledger.getExtract(request.getAccount()), LedgerRequestType.GET_EXTRACT);
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
                            System.out.println("ENTROU NO GET_BLOCK");
                            rep = new Reply(this.ledger.getBlockToMine(), LedgerRequestType.GET_BLOCK_TO_MINE);
                            System.out.println("SAIU DO GET_BLOCK");
                            break;
                        case MINE_BLOCK:
                            System.out.println("ENTROU NO MINE_BLOCK");
                            this.ledger.addMinedBlock(request.getBlock());
                            Request rewardRequest = new Request(LedgerRequestType.LOAD_MONEY, this.LEDGER, request.getAccount(), REWARD, -1);
                            rewardRequest.setPublicKey(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                            rewardRequest.setSignature(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.LOAD_MONEY.toString().getBytes()));
                            this.sendTransaction(rewardRequest);
                            rep = new Reply(REWARD, request.getRequestType());
                            System.out.println("SAIU DO MINE_BLOCK");
                            break;
                        default:
                            rep = new Reply("Operation not supported");
                    }
                } else {
                    rep = new Reply("Bad signature");
                }

                rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), request.getRequestType().toString().getBytes()));
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
                repError.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                repError.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.ERROR.toString().getBytes()));
                objOut.writeObject(repError);
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            } catch (IOException ex) {
                e.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
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
                        rep = new Reply(this.ledger.getExtract(request.getAccount()), LedgerRequestType.GET_EXTRACT);
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
                    default:
                        rep = new Reply("Operation not supported");
                }
            } else {
                rep = new Reply("Bad signature");
            }
            rep.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
            rep.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), request.getRequestType().toString().getBytes()));

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
                repError.setPublicKeyReplica(serviceReplica.getReplicaContext().getStaticConfiguration().getPublicKey().getEncoded());
                repError.setSignatureReplica(Security.signRequest(serviceReplica.getReplicaContext().getStaticConfiguration().getPrivateKey(), LedgerRequestType.ERROR.toString().getBytes()));
                objOut.writeObject(repError);
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            } catch (IOException ex) {
                e.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
        return reply;
    }

    @Override
    public byte[] getSnapshot() {
        //return LedgerDataStructure.serialize(ledger.fromJedis());
        return new byte[0];
    }



    @Override
    public void installSnapshot(byte[] state) {
        //this.ledger.toJedis(LedgerDataStructure.deserialize(state));
    }
}
