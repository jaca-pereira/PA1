package proxy;


import bftsmart.tom.ServiceProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public byte[]  createAccount(Data data) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.CREATE_ACCOUNT);
            objOut.writeObject(data.getAccount());
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
    public void loadMoney(Data data) {

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.LOAD_MONEY);
            objOut.writeObject(data.getAccount());
            objOut.writeObject(data.getValue());
            objOut.flush();
            byteOut.flush();

            serviceProxy.invokeOrdered(byteOut.toByteArray());

        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    @Override
    public int getBalance(Data data) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.GET_BALANCE);
            objOut.writeObject(data.getAccount());
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
    public List<Transaction> getExtract(Data data) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.GET_EXTRACT);
            objOut.writeObject(data.getAccount());
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
    public void sendTransaction(Data data) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.SEND_TRANSACTION);
            objOut.writeObject(data.getAccount());
            objOut.writeObject(data.getAccountDestiny());
            objOut.writeObject(data.getValue());
            objOut.writeObject(data.getNonce());
            objOut.flush();
            byteOut.flush();

            serviceProxy.invokeOrdered(byteOut.toByteArray());

        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    @Override
    public int getTotalValue(Data data) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.GET_TOTAL_VALUE);
            objOut.writeObject(data.getAccounts());
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
    public int getGlobalValue(Data data) {
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
    public String getLedger() {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(LedgerRequestType.GET_LEDGER);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Map<byte[], List<Transaction>> response = (Map<byte[], List<Transaction>>) objIn.readObject();
                ObjectMapper mapper = new ObjectMapper();
                String jsonResult = mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(response);
                return  jsonResult;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }
}
