package proxy;


import bftsmart.tom.ServiceProxy;

import data.Transaction;

import java.io.*;
import java.util.List;
import java.util.Map;


public class Service implements ServiceAPI {
    ServiceProxy serviceProxy;

    public Service(int clientId) {
        serviceProxy = new ServiceProxy(clientId);
    }

    @Override
    public String home() {
        return "Hello World";
    }

    @Override
    public byte[]  createAccount(byte[] data) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.CREATE_ACCOUNT);
            objOut.writeObject(Data.deserialize(data).getAccount());
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
    public boolean loadMoney(byte[] data) {

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.LOAD_MONEY);

            objOut.writeObject(Data.deserialize(data).getAccount());
            objOut.writeObject(Data.deserialize(data).getValue());
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                return (boolean) objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return false;
    }

    @Override
    public int getBalance(byte[] data) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.GET_BALANCE);
            objOut.writeObject(Data.deserialize(data).getAccount());
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
    public List<Transaction> getExtract(byte[] data) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.GET_EXTRACT);
            objOut.writeObject(Data.deserialize(data).getAccount());
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
    public boolean sendTransaction(byte[] data) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.SEND_TRANSACTION);
            objOut.writeObject(Data.deserialize(data).getAccount());
            objOut.writeObject(Data.deserialize(data).getAccountDestiny());
            objOut.writeObject(Data.deserialize(data).getValue());
            objOut.writeObject(Data.deserialize(data).getNonce());
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                return (boolean) objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return false;
    }

    @Override
    public int getTotalValue(byte[] data) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.GET_TOTAL_VALUE);
            objOut.writeObject(Data.deserialize(data).getAccounts());
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
    public int getGlobalValue(byte[] data) {
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
    public Map<String, List<Transaction>> getLedger() {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.GET_LEDGER);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {

                return  (Map<String, List<Transaction>>) objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }
}
