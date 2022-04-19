package data;

public class Transaction {
    private byte[] originalAccount;
    private byte[] destinationAccount;
    private int value;
    private long nonce;

    public Transaction(byte[]  originalAccount, byte[]  destinationAccount, int value, long nonce) {
        this.originalAccount = originalAccount;
        this.destinationAccount = destinationAccount;
        this.value = value;
        this.nonce = nonce;
    }

    public byte[] getOriginalAccount() {
        return this.originalAccount;
    }

    public byte[]  getDestinationAccount() {
        return this.destinationAccount;
    }

    public int getValue() {
        return this.value;
    }

    public long getNonce() {
        return this.nonce;
    }



}
