package proxy;

import java.io.Serializable;
import java.util.List;

public class Data implements Serializable {

    private byte[] signature;
    private byte[] account;
    private byte[] accountDestiny;
    private  int value;
    private long nonce;
    private List<byte[]> accounts;

    public Data(byte[] signature, byte[] account, byte[] accountDestiny, int value, long nonce) {
        this.signature = signature;
        this.account = account;
        this.accountDestiny = accountDestiny;
        this.value = value;
        this.nonce = nonce;
    }

    public Data(byte[] signature, byte[] account) {
        this.signature = signature;
        this.account = account;
    }

    public Data(byte[] signature, byte[] account, int value) {
        this.signature = signature;
        this.account = account;
        this.value = value;
    }

    public Data(byte[] signature) {
        this.signature = signature;
    }

    public Data(byte[] signature, List<byte[]> accounts) {
        this.signature = signature;
        this.accounts = accounts;
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




}
