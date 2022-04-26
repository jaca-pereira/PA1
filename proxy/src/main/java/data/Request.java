package data;


import java.io.*;
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

    public Request(LedgerRequestType requestType, byte[] account, int value) {
        this.requestType = requestType;
        this.account = account;
        this.value = value;
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

    public byte[] getPublicKey() {
        return publicKey;
    }
    public byte[] getSignature() {
        return signature;
    }

    public byte[] getAccount() {
        return account;
    }


    public byte[] getAccountDestiny() {
        return accountDestiny;
    }

    public int getValue() {
        return value;
    }

    public long getNonce() {
        return nonce;
    }

    public List<byte[]> getAccounts() {
        return accounts;
    }


    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }
}