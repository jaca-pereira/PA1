package data;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.List;

public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;
    private long nonce;
    private byte[] id;
    private LedgerRequestType transactionType;
    private byte[] originAccount;
    private byte[] destinationAccount;
    private int value;
    private byte[] sig;

    private List<byte[]> accounts;

    public Transaction(LedgerRequestType transactionType, byte[] originAccount, byte[] sig) {
        this.originAccount = originAccount;
        this.transactionType = transactionType;
        this.nonce = -1;
        this.destinationAccount=null;
        this.value = -1;
        this.accounts = null;
        this.sig = sig;
    }

    public Transaction(LedgerRequestType transactionType, byte[] sig) {
        this.sig = sig;
        this.nonce = -1;
        this.accounts = null;
        this.originAccount = null;
        this.destinationAccount = null;
        this.value = -1;
        this.transactionType = transactionType;
    }

    public Transaction(LedgerRequestType transactionType, List<byte[]> accounts, byte[] sig) {
        this.accounts = accounts;
        this.transactionType = transactionType;
        this.originAccount = null;
        this.nonce = -1;
        this.destinationAccount=null;
        this.value = -1;
        this.sig = sig;
    }

    public Transaction(LedgerRequestType transactionType, byte[] originAccount, byte[] destinationAccount, int value, long nonce, byte[] sig) {
        this.transactionType = transactionType;
        this.originAccount = originAccount;
        this.destinationAccount = destinationAccount;
        this.value = value;
        this.nonce = nonce;
        this.id = this.idGenerator();
        this.accounts = null;
        this.sig = sig;
    }

    private byte[] idGenerator() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        byte[] nonce = buffer.putLong(this.nonce).array();

        ByteBuffer id = ByteBuffer.wrap(new byte[nonce.length + this.originAccount.length]);
        id.put(nonce);
        id.put(this.originAccount);
        return id.array();
    }

    public LedgerRequestType getTransactionType() {
        return this.transactionType;
    }
    public byte[] getOriginAccount() {
        return this.originAccount;
    }

    public byte[] getDestinationAccount() {
        return this.destinationAccount;
    }

    public int getValue() {
        return this.value;
    }

    public List<byte[]> getAccounts() {
        return this.accounts;
    }

    public long getNonce() {
        return nonce;
    }

    public byte[] getId() {
        return this.id;
    }

    public byte[] getSig() {
        return sig;
    }

    public static byte[] serialize(Transaction obj) {
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

    public static Transaction deserialize(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return (Transaction) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
