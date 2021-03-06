package data;


import Security.Security;

import java.io.*;
import java.security.PublicKey;
import java.util.List;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private LedgerRequestType requestType;
    private byte[] publicKey;
    private byte[] signature;
    private byte[] account;
    private byte[] accountDestiny;
    private  int value;
    private long nonce;
    private List<byte[]> accounts;
    private Block block;


    public Request(LedgerRequestType requestType, byte[] account, byte[] accountDestiny, int value, long nonce) {
        this.requestType = requestType;
        this.account = account;
        this.accountDestiny = accountDestiny;
        this.value = value;
        this.nonce = nonce;
        this.publicKey = null;
        this.signature = null;
    }

    public Request(LedgerRequestType requestType, byte[] account) {
        this.requestType = requestType;
        this.account = account;
        this.publicKey = null;
        this.signature = null;
    }
    public Request(LedgerRequestType requestType, byte[] account, Block block) {
        this.requestType = requestType;
        this.block = block;
        this.account = account;
        this.publicKey = null;
        this.signature = null;
    }

    public Request(LedgerRequestType requestType, byte[] account, int begin) {
        this.requestType = requestType;
        this.account = account;
        this.value = begin;
        this.publicKey = null;
        this.signature = null;
    }

    public Request(LedgerRequestType requestType, List<byte[]> accounts) {
        this.requestType = requestType;
        this.accounts = accounts;
        this.publicKey = null;
        this.signature = null;
    }

    public Request(LedgerRequestType requestType) {
        this.publicKey = null;
        this.signature = null;
        this.requestType = requestType;
    }

    public static byte[] serialize(Request obj) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(obj);
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Request deserialize(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return (Request) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public LedgerRequestType getRequestType() {
        return this.requestType;
    }

    public PublicKey getPublicKey() {
        return Security.getPublicKey(this.publicKey);
    }

    public byte[] getSignature() {
        return this.signature;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }


}
