package proxy;


import Security.Security;
import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;

import bftsmart.tom.core.messages.TOMMessageType;
import bftsmartimp.ReplyListenerImp;
import blockmess.ApplicationInterfaceImp;
import blockmess.ReplyListenerBlockmess;
import data.*;

import javax.ws.rs.InternalServerErrorException;
import java.io.*;

import java.net.URI;
import java.security.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


public class Service implements ServiceAPI {
    private Client client;

    public Service(URI proxyURI) throws NoSuchAlgorithmException {
        this.client = new Client(proxyURI);
    }

    private byte[] sendRequest(byte[] request) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
            Request deserialized = Request.deserialize(request);
            if (Security.verifySignature(deserialized.getPublicKey(), deserialized.getRequestType().toString().getBytes(), deserialized.getSignature())) {
                return this.client.sendTransaction(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] sendTransaction(byte[] request) {
        return this.sendRequest(request);
    }
}
