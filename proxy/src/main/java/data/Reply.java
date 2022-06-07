package data;

import Security.Security;

import java.io.*;
import java.security.PublicKey;
import java.util.List;

public class Reply implements Serializable {

    private static final long serialVersionUID = 1L;
    //Security stuff
    private byte[] publicKey;
    private byte[] signature;
    //Response stuff
    private boolean boolReply;
    private byte[] byteReply;
    private int intReply;
    private List<Transaction> listReply;

    public Reply(boolean boolReply) {
        this.boolReply = boolReply;
        this.publicKey = null;
        this.signature = null;
    }
    public Reply(byte[] byteReply) {
        this.publicKey = null;
        this.signature = null;
        this.byteReply = byteReply;
    }

    public Reply(int intReply) {
        this.publicKey = null;
        this.signature = null;
        this.intReply = intReply;
    }

    public Reply(List<Transaction> listReply) {
        this.publicKey = null;
        this.signature = null;
        this.listReply = listReply;
    }

    public boolean getBoolReply() {
        return this.boolReply;
    }

    public byte[] getByteReply() {
        return this.byteReply;
    }

    public int getIntReply() {
        return this.intReply;
    }

    public List<Transaction> getListReply() {
        return this.listReply;
    }

    public PublicKey getPublicKey() {
        return Security.getPublicKey(this.publicKey);
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public static byte[] serialize(Reply obj) {
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

    public static Reply deserialize(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return (Reply) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
