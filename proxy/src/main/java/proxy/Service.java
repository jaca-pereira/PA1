package proxy;


import Security.Security;
import bftsmart.tom.ServiceProxy;
import data.Request;
import data.LedgerRequestType;
import data.Reply;

import java.io.*;
import java.security.KeyPair;


public class Service implements ServiceAPI {
    ServiceProxy serviceProxy;

    private KeyPair keyPair;

    public Service(int clientId) {
        this.keyPair = Security.getKeyPair();
        serviceProxy = new ServiceProxy(clientId);
    }

    @Override
    public String home() {
        return "Hello World";
    }

    @Override
    public byte[] createAccount(byte[] request) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(Request.deserialize(request));
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                return Reply.serialize((Reply) objIn.readObject());
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;

    }

    @Override
    public byte[] loadMoney(byte[] request) {

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(Request.deserialize(request));
            objOut.flush();
            byteOut.flush();
            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                return Reply.serialize((Reply) objIn.readObject());

            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public byte[] getBalance(byte[] request) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(Request.deserialize(request));
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Reply rep = (Reply) objIn.readObject();
                rep.setPublicKey(this.keyPair.getPublic().getEncoded());
                rep.setSignature(Security.signRequest(this.keyPair.getPrivate(), request));
                return Reply.serialize(rep);

            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public byte[] getExtract(byte[] request) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(Request.deserialize(request));
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Reply rep = (Reply) objIn.readObject();
                rep.setPublicKey(this.keyPair.getPublic().getEncoded());
                rep.setSignature(Security.signRequest(this.keyPair.getPrivate(), request));
                return Reply.serialize(rep);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public byte[] sendTransaction(byte[] request) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(Request.deserialize(request));
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Reply rep = (Reply) objIn.readObject();
                rep.setPublicKey(this.keyPair.getPublic().getEncoded());
                rep.setSignature(Security.signRequest(this.keyPair.getPrivate(), request));
                return Reply.serialize(rep);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public byte[] getTotalValue(byte[] request) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(Request.deserialize(request));
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Reply rep = (Reply) objIn.readObject();
                rep.setPublicKey(this.keyPair.getPublic().getEncoded());
                rep.setSignature(Security.signRequest(this.keyPair.getPrivate(), request));
                return Reply.serialize(rep);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public byte[] getGlobalValue(byte[] request) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(Request.deserialize(request));
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Reply rep = (Reply) objIn.readObject();
                rep.setPublicKey(this.keyPair.getPublic().getEncoded());
                rep.setSignature(Security.signRequest(this.keyPair.getPrivate(), request));
                return Reply.serialize(rep);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
        }

        @Override
    public byte[] getLedger() {

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(new Request(LedgerRequestType.GET_LEDGER));
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                return Reply.serialize((Reply) objIn.readObject());
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }
}
