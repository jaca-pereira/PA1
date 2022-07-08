package data;

import java.io.*;
import java.nio.ByteBuffer;


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
