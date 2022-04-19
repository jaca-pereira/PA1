package replicas;


import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import data.Ledger;
import data.Transaction;
import proxy.LedgerRequestType;

import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LedgerReplica extends DefaultSingleRecoverable {

    private Ledger ledger;
    private Logger logger;


    public LedgerReplica(int id) {
        ledger = new Ledger();
        logger = Logger.getLogger(LedgerReplica.class.getName());
        new ServiceReplica(0, this, this);
    }

    public byte[]  createAccount(byte[] account, byte[] signature) {
        return this.ledger.addAccount(account);
    }

    public void loadMoney(byte[] account, int value, byte[] signature) {
        if (value <= 0)
            throw new IllegalArgumentException("Value must be positive!");
        this.ledger.sendTransaction(Ledger.LEDGER, account, value, -1);
    }

    public int getBalance(byte[] account, byte[] signature) {
        return this.ledger.getBalance(account);
    }

    public List<Transaction> getExtract(byte[] account, byte[] signature) {
        return this.ledger.getExtract(account);
    }

    public void sendTransaction(byte[] originAccount, byte[] destinationAccount, int value, byte[] signature, long nonce) {
        if (value <= 0)
            throw new IllegalArgumentException("Value must be positive!");
        //verificar nonce?
        //verificar signature neste e em todos
        this.ledger.sendTransaction(originAccount, destinationAccount, value, nonce);
    }

    public int getTotalValue(List<byte[]> accounts, byte[] signature) {
        return this.ledger.getTotalValue(accounts);
    }

    public int getGlobalValue(byte[] signature) {
        return this.ledger.getGlobalValue();
    }

    public Ledger getLedger() {
        return this.ledger;
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
                    byte[] account1 = (byte[] )objIn.readObject();
                    this.createAccount(account1,new byte[]{});
                    objOut.writeObject(account1);
                    hasReply = true;
                    break;
                case LOAD_MONEY:
                    byte[] account2 = (byte[] )objIn.readObject();
                    int value = (int)objIn.readObject();
                    this.loadMoney(account2, value,new byte[]{});
                    break;
                case GET_BALANCE:
                    byte[] account3 = (byte[] )objIn.readObject();
                    int balance = this.getBalance(account3, new byte[]{});
                    objOut.writeObject(balance);
                    hasReply = true;
                    break;
                case GET_EXTRACT:
                    byte[] account4 = (byte[] )objIn.readObject();
                    List<Transaction> extract = this.getExtract(account4, new byte[]{});
                    objOut.writeObject(extract);
                    hasReply = true;
                    break;
                case SEND_TRANSACTION:
                    byte[] account5 = (byte[] )objIn.readObject();
                    byte[] account6 = (byte[] )objIn.readObject();
                    int amount = (int )objIn.readObject();
                    long nonce = (long )objIn.readObject();
                    this.sendTransaction(account5, account6, amount, new byte[]{}, nonce);
                    break;
                case GET_TOTAL_VALUE:
                    List<byte[]> accounts = (List<byte[]>) objIn.readObject();
                    int totalValue = this.getTotalValue(accounts, new byte[]{});
                    objOut.writeObject(totalValue);
                    hasReply = true;
                    break;
                case GET_GLOBAL_VALUE:
                    int globalValue = this.getGlobalValue(new byte[]{});
                    objOut.writeObject(globalValue);
                    hasReply = true;
                    break;
                case GET_LEDGER:
                    objOut.writeObject(this.ledger);
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
                case LOAD_MONEY:
                    byte[] account = (byte[] )objIn.readObject();
                    int value = (int)objIn.readObject();
                    this.loadMoney(account, value,new byte[]{});
                    break;
                case GET_BALANCE:
                    account = (byte[] )objIn.readObject();
                    int balance = this.getBalance(account, new byte[]{});
                    objOut.writeObject(balance);
                    hasReply = true;
                    break;
                case GET_EXTRACT:
                    account = (byte[] )objIn.readObject();
                    List<Transaction> extract = this.getExtract(account, new byte[]{});
                    objOut.writeObject(extract);
                    hasReply = true;
                    break;
                case GET_TOTAL_VALUE:
                    List<byte[]> accounts = (List<byte[]>) objIn.readObject();
                    int totalValue = this.getTotalValue(accounts, new byte[]{});
                    objOut.writeObject(totalValue);
                    hasReply = true;
                case GET_GLOBAL_VALUE:
                    totalValue = this.getGlobalValue(new byte[]{});
                    objOut.writeObject(totalValue);
                    hasReply = true;
                    break;
                case GET_LEDGER:
                    objOut.writeObject(this.ledger);
                    hasReply=true;
                    break;
                default:
                    throw new UnsupportedOperationException("Operation does not exist or is not supported in unordered mode!");
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
