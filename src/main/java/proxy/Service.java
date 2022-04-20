package proxy;


import bftsmart.tom.ServiceProxy;
import data.Ledger;
import data.Transaction;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.List;


public class Service implements ServerAPI{
    ServiceProxy serviceProxy;

    public Service(int clientId) {
        serviceProxy = new ServiceProxy(clientId);
    }

    @Override
    public String home() {
        return "Hello Docker World";
    }

    @Override
    public byte[]  createAccount(byte[] account, byte[] signature) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.CREATE_ACCOUNT);
            objOut.writeObject(account);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                return (byte[]) objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;

    }

    @Override
    public void loadMoney(byte[] account, int value, byte[] signature) {

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.LOAD_MONEY);
            objOut.writeObject(account);
            objOut.writeObject(value);
            objOut.flush();
            byteOut.flush();

            serviceProxy.invokeOrdered(byteOut.toByteArray());

        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    @Override
    public int getBalance(byte[] account, byte[] signature) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.GET_BALANCE);
            objOut.writeObject(account);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                return (int) objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public List<Transaction> getExtract(byte[] account, byte[] signature) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.GET_EXTRACT);
            objOut.writeObject(account);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                return (List<Transaction>) objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void sendTransaction(byte[] originAccount, byte[] destinationAccount, int value, byte[] signature, long nonce) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.SEND_TRANSACTION);
            objOut.writeObject(originAccount);
            objOut.writeObject(destinationAccount);
            objOut.writeObject(value);
            objOut.writeObject(nonce);
            objOut.flush();
            byteOut.flush();

            serviceProxy.invokeOrdered(byteOut.toByteArray());

        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    @Override
    public int getTotalValue(List<byte[]> accounts, byte[] signature) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.GET_TOTAL_VALUE);
            objOut.writeObject(accounts);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                return (int) objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public int getGlobalValue(byte[] signature) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.GET_GLOBAL_VALUE);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                return (int) objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public Ledger getLedger() {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.GET_EXTRACT);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                return (Ledger) objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }
}
