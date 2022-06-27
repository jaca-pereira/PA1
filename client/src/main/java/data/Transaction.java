package data;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.List;

public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;
    private long nonce;
    private byte[] id;
    private byte[] originAccount;
    private byte[] destinationAccount;
    private int value;
    private byte[] sig;

    public Transaction(byte[] originAccount, byte[] destinationAccount, long nonce, int value, byte[] sig) {
        this.originAccount = originAccount;
        this.nonce = nonce;
        this.destinationAccount = destinationAccount;
        this.value = value;
        this.sig = sig;
        this.id = this.generateId();
    }

    private byte[] generateId() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        byte[] nonce = buffer.putLong(this.nonce).array();
        ByteBuffer id = ByteBuffer.wrap(new byte[nonce.length + this.originAccount.length]);
        id.put(nonce);
        id.put(this.originAccount);
        return id.array();
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

    public String toString() {
        String result = "";
        result+= "nonce: " + nonce +"\n";
        result+= "id: " + id +"\n";
        result+= "originAccount: " + new String(originAccount) +"\n";
        result+= "destinationAccount: " + new String(destinationAccount) +"\n";
        result+= "value: " + value +"\n";
        result+= "signature: " + new String(sig) +"\n";
        return result;
    }
}
