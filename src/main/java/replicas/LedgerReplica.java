package replicas;


import Security.Security;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import bftsmart.tom.util.TOMUtil;
import data.Ledger;
import data.Transaction;
import org.apache.log4j.BasicConfigurator;
import proxy.LedgerRequestType;

import java.io.*;
import java.security.KeyPair;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LedgerReplica extends DefaultSingleRecoverable {

    private Ledger ledger;
    private Logger logger;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: LedgerReplica <id>");
            System.exit(-1);
        }
        new LedgerReplica(Integer.parseInt(args[0]));
    }

    public LedgerReplica(int id) {
        ledger = new Ledger();
        logger = Logger.getLogger(LedgerReplica.class.getName());
        BasicConfigurator.configure();
        new ServiceReplica(id, this, this);
    }

    public byte[]  createAccount(byte[] publicKey, byte[] signature, byte[] account) {

        if(!Security.verifySignature(Security.getPublicKey(publicKey),"CREATE_ACCOUNT".getBytes(), signature))
            throw new IllegalArgumentException("Signature not valid!");
        return this.ledger.addAccount(account);
    }

    public boolean loadMoney(byte[] publicKey, byte[] signature, byte[] account, int value) {
        if(!Security.verifySignature(Security.getPublicKey(publicKey),"LOAD_MONEY".getBytes(), signature))
            throw new IllegalArgumentException("Signature not valid!");
        if (value <= 0)
            throw new IllegalArgumentException("Value must be positive!");
        this.ledger.sendTransaction(Ledger.LEDGER, account, value, -1);
        return true;
    }

    public int getBalance(byte[] publicKey, byte[] signature, byte[] account) {
        if(!Security.verifySignature(Security.getPublicKey(publicKey),"GET_BALANCE".getBytes(), signature))
            throw new IllegalArgumentException("Signature not valid!");
        return this.ledger.getBalance(account);
    }

    public List<Transaction> getExtract(byte[] publicKey, byte[] signature, byte[] account) {
        if(!Security.verifySignature(Security.getPublicKey(publicKey),"GET_EXTRACT".getBytes(), signature))
            throw new IllegalArgumentException("Signature not valid!");
        return this.ledger.getExtract(account);
    }

    public boolean sendTransaction(byte[] publicKey, byte[] signature, byte[] originAccount, byte[] destinationAccount, int value, long nonce) {
        if(!Security.verifySignature(Security.getPublicKey(publicKey),"SEND_TRANSACTION".getBytes(), signature))
            throw new IllegalArgumentException("Signature not valid!");
        if (value <= 0)
            throw new IllegalArgumentException("Value must be positive!");
        //verificar nonce?
        this.ledger.sendTransaction(originAccount, destinationAccount, value, nonce);
        return true;
    }

    public int getTotalValue(byte[] publicKey, byte[] signature, List<byte[]> accounts) {
        if(!Security.verifySignature(Security.getPublicKey(publicKey),"GET_TOTAL_VALUE".getBytes(), signature))
            throw new IllegalArgumentException("Signature not valid!");
        return this.ledger.getTotalValue(accounts);
    }

    public int getGlobalValue(byte[] publicKey, byte[] signature) {
        if(!Security.verifySignature(Security.getPublicKey(publicKey),"GET_GLOBAL_VALUE".getBytes(), signature))
            throw new IllegalArgumentException("Signature not valid!");
        return this.ledger.getGlobalValue();
    }

    public Map<String, List<Transaction>> getLedger() {
        return this.ledger.getLedger();
    }

    // bft

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        byte[] reply = null;
        Ledger l;
        boolean hasReply = false;
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
            LedgerRequestType reqType = (LedgerRequestType)objIn.readObject();
            switch (reqType) {
                case CREATE_ACCOUNT:
                    byte[] publicKey = (byte[])objIn.readObject();
                    byte[] signature = (byte[])objIn.readObject();
                    byte[] account1 = (byte[])objIn.readObject();
                    account1 = this.createAccount(publicKey, signature, account1);
                    objOut.writeObject(account1);
                    hasReply = true;
                    break;
                case LOAD_MONEY:
                    byte[] publicKey1 = (byte[])objIn.readObject();
                    byte[] signature1 = (byte[])objIn.readObject();
                    byte[] account2 = (byte[] )objIn.readObject();
                    int value = (int)objIn.readObject();
                    boolean ok= this.loadMoney(publicKey1, signature1, account2, value);
                    objOut.writeObject(ok);
                    hasReply = true;
                    break;
                case SEND_TRANSACTION:
                    byte[] publicKey4 = (byte[])objIn.readObject();
                    byte[] signature4 = (byte[])objIn.readObject();
                    byte[] account5 = (byte[] )objIn.readObject();
                    byte[] account6 = (byte[] )objIn.readObject();
                    int amount = (int )objIn.readObject();
                    long nonce = (long )objIn.readObject();
                    boolean ok2 = this.sendTransaction(publicKey4, signature4, account5, account6, amount, nonce);
                    objOut.writeObject(ok2);
                    hasReply = true;
                    break;
                case GET_BALANCE:
                    byte[] publicKey2 = (byte[])objIn.readObject();
                    byte[] signature2 = (byte[])objIn.readObject();
                    byte[] account3 = (byte[] )objIn.readObject();
                    int balance = this.getBalance(publicKey2, signature2, account3);
                    objOut.writeObject(balance);
                    hasReply = true;
                    break;
                case GET_EXTRACT:
                    byte[] publicKey3 = (byte[])objIn.readObject();
                    byte[] signature3 = (byte[])objIn.readObject();
                    byte[] account4 = (byte[] )objIn.readObject();
                    List<Transaction> extract = this.getExtract(publicKey3, signature3, account4);
                    objOut.writeObject(extract);
                    hasReply = true;
                    break;
                case GET_TOTAL_VALUE:
                    byte[] publicKey5 = (byte[])objIn.readObject();
                    byte[] signature5 = (byte[])objIn.readObject();
                    List<byte[]> accounts = (List<byte[]>) objIn.readObject();
                    int totalValue = this.getTotalValue(publicKey5, signature5, accounts);
                    objOut.writeObject(totalValue);
                    hasReply = true;
                    break;
                case GET_GLOBAL_VALUE:
                    byte[] publicKey6 = (byte[])objIn.readObject();
                    byte[] signature6 = (byte[])objIn.readObject();
                    int globalValue = this.getGlobalValue(publicKey6, signature6);
                    objOut.writeObject(globalValue);
                    hasReply = true;
                    break;


                case GET_LEDGER:
                    objOut.writeObject(this.getLedger());
                    hasReply=true;
                    break;
                default:
                    throw new UnsupportedOperationException("Operation does not exist!");
            }
            if (hasReply) {
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            } else {
                reply = new byte[0];
            }

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Ocurred during operation execution", e);
        }
        return reply;
    }

    @SuppressWarnings("unchecked")
    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        byte[] reply = null;
        Ledger l;
        boolean hasReply = false;

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
            LedgerRequestType reqType = (LedgerRequestType)objIn.readObject();
            switch (reqType) {
                case GET_BALANCE:
                    byte[] publicKey2 = (byte[])objIn.readObject();
                    byte[] signature2 = (byte[])objIn.readObject();
                    byte[] account3 = (byte[] )objIn.readObject();
                    int balance = this.getBalance(publicKey2, signature2, account3);
                    objOut.writeObject(balance);
                    hasReply = true;
                    break;
                case GET_EXTRACT:
                    byte[] publicKey3 = (byte[])objIn.readObject();
                    byte[] signature3 = (byte[])objIn.readObject();
                    byte[] account4 = (byte[] )objIn.readObject();
                    List<Transaction> extract = this.getExtract(publicKey3, signature3, account4);
                    objOut.writeObject(extract);
                    hasReply = true;
                    break;
                case GET_TOTAL_VALUE:
                    byte[] publicKey5 = (byte[])objIn.readObject();
                    byte[] signature5 = (byte[])objIn.readObject();
                    List<byte[]> accounts = (List<byte[]>) objIn.readObject();
                    int totalValue = this.getTotalValue(publicKey5, signature5, accounts);
                    objOut.writeObject(totalValue);
                    hasReply = true;
                    break;
                case GET_GLOBAL_VALUE:
                    byte[] publicKey6 = (byte[])objIn.readObject();
                    byte[] signature6 = (byte[])objIn.readObject();
                    int globalValue = this.getGlobalValue(publicKey6, signature6);
                    objOut.writeObject(globalValue);
                    hasReply = true;
                    break;


                case GET_LEDGER:
                    objOut.writeObject(this.getLedger());
                    hasReply=true;
                    break;
                default:
                    throw new UnsupportedOperationException("Operation does not exist!");
            }
            if (hasReply) {
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            } else {
                reply = new byte[0];
            }
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
