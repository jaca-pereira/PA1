package proxy;


import Security.Security;
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

            Data dataDeserialized = Data.deserialize(data);

            if(!Security.verifySignature(Security.getPublicKey(dataDeserialized.getPublicKey()),"CREATE_ACCOUNT".getBytes(), dataDeserialized.getSignature()))
                throw new IllegalArgumentException("Signature not valid!");

            objOut.writeObject(LedgerRequestType.CREATE_ACCOUNT);
            objOut.writeObject(dataDeserialized.getPublicKey());
            objOut.writeObject(dataDeserialized.getSignature());
            objOut.writeObject(dataDeserialized.getAccount());
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


            Data dataDeserialized = Data.deserialize(data);

            if(!Security.verifySignature(Security.getPublicKey(dataDeserialized.getPublicKey()),"LOAD_MONEY".getBytes(), dataDeserialized.getSignature()))
                throw new IllegalArgumentException("Signature not valid!");

            objOut.writeObject(LedgerRequestType.LOAD_MONEY);
            objOut.writeObject(dataDeserialized.getPublicKey());
            objOut.writeObject(dataDeserialized.getSignature());
            objOut.writeObject(dataDeserialized.getAccount());
            objOut.writeObject(dataDeserialized.getValue());
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

            Data dataDeserialized = Data.deserialize(data);

            if(!Security.verifySignature(Security.getPublicKey(dataDeserialized.getPublicKey()),"GET_BALANCE".getBytes(), dataDeserialized.getSignature()))                throw new IllegalArgumentException("Signature not valid!");

            objOut.writeObject(LedgerRequestType.GET_BALANCE);
            objOut.writeObject(dataDeserialized.getPublicKey());
            objOut.writeObject(dataDeserialized.getSignature());
            objOut.writeObject(dataDeserialized.getAccount());
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

            Data dataDeserialized = Data.deserialize(data);

            if(!Security.verifySignature(Security.getPublicKey(dataDeserialized.getPublicKey()),"GET_EXTRACT".getBytes(), dataDeserialized.getSignature()))                throw new IllegalArgumentException("Signature not valid!");
            objOut.writeObject(LedgerRequestType.GET_EXTRACT);
            objOut.writeObject(dataDeserialized.getPublicKey());
            objOut.writeObject(dataDeserialized.getSignature());
            objOut.writeObject(dataDeserialized.getAccount());
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

            Data dataDeserialized = Data.deserialize(data);

            if(!Security.verifySignature(Security.getPublicKey(dataDeserialized.getPublicKey()),"SEND_TRANSACTION".getBytes(), dataDeserialized.getSignature()))                throw new IllegalArgumentException("Signature not valid!");

            objOut.writeObject(LedgerRequestType.SEND_TRANSACTION);
            objOut.writeObject(dataDeserialized.getPublicKey());
            objOut.writeObject(dataDeserialized.getSignature());
            objOut.writeObject(dataDeserialized.getAccount());
            objOut.writeObject(dataDeserialized.getAccountDestiny());
            objOut.writeObject(dataDeserialized.getValue());
            objOut.writeObject(dataDeserialized.getNonce());
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

            Data dataDeserialized = Data.deserialize(data);

            if(!Security.verifySignature(Security.getPublicKey(dataDeserialized.getPublicKey()),"GET_TOTAL_VALUE".getBytes(), dataDeserialized.getSignature()))                throw new IllegalArgumentException("Signature not valid!");

            objOut.writeObject(LedgerRequestType.GET_TOTAL_VALUE);
            objOut.writeObject(dataDeserialized.getPublicKey());
            objOut.writeObject(dataDeserialized.getSignature());
            objOut.writeObject(dataDeserialized.getAccounts());
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

            Data dataDeserialized = Data.deserialize(data);

            if(!Security.verifySignature(Security.getPublicKey(dataDeserialized.getPublicKey()),"GET_GLOBAL_VALUE".getBytes(), dataDeserialized.getSignature()))                throw new IllegalArgumentException("Signature not valid!");

            objOut.writeObject(LedgerRequestType.GET_GLOBAL_VALUE);
            objOut.writeObject(dataDeserialized.getPublicKey());
            objOut.writeObject(dataDeserialized.getSignature());
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
                Map<String, List<Transaction>> map = (Map<String, List<Transaction>>)objIn.readObject();
                byte[] signature = (byte[]) objIn.readObject();
                return  map;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }
}
