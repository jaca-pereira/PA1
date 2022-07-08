package data;

import Security.Security;

import java.io.*;
import java.security.PublicKey;
import java.util.List;

public class Reply implements Serializable {

    private static final long serialVersionUID = 1L;

    //Security stuff
    private byte[] publicKeyProxy;
    private byte[] signatureProxy;

    private byte[] publicKeyReplica;
    private byte[] signatureReplica;
    //Response stuff
    private boolean boolReply;
    private byte[] byteReply;
    private int intReply;
    private List listReply;

    private LedgerRequestType requestType;

    private Block blockReply;



    private Error error;
    public Reply(String error) {

        this.error = new Error(error);
        this.requestType = LedgerRequestType.ERROR;
    }
    public Reply(boolean boolReply, LedgerRequestType requestType) {
        this.boolReply = boolReply;
        this.publicKeyProxy = null;
        this.signatureProxy = null;
        this.publicKeyReplica = null;
        this.signatureReplica = null;
        this.requestType = requestType;
    }
    public Reply(byte[] byteReply, LedgerRequestType requestType) {
        this.publicKeyProxy = null;
        this.signatureProxy = null;
        this.publicKeyReplica = null;
        this.signatureReplica = null;
        this.byteReply = byteReply;
        this.requestType = requestType;
    }

    public Reply(int intReply, LedgerRequestType requestType) {
        this.publicKeyProxy = null;
        this.signatureProxy = null;
        this.publicKeyReplica = null;
        this.signatureReplica = null;
        this.intReply = intReply;
        this.requestType = requestType;
    }

    public Reply(List listReply, LedgerRequestType requestType) {
        this.publicKeyProxy = null;
        this.signatureProxy = null;
        this.publicKeyReplica = null;
        this.signatureReplica = null;
        this.listReply = listReply;
        this.requestType = requestType;
    }
    public Reply(Block blockReply, LedgerRequestType requestType) {
        this.publicKeyProxy = null;
        this.signatureProxy = null;
        this.publicKeyReplica = null;
        this.signatureReplica = null;
        this.blockReply = blockReply;
        this.requestType = requestType;
    }

    public Reply(LedgerRequestType requestType) {
        this.requestType = requestType;
        this.publicKeyProxy = null;
        this.signatureProxy = null;
        this.publicKeyReplica = null;
        this.signatureReplica = null;
    }


    public void setPublicKeyProxy(byte[] publicKeyProxy) {
        this.publicKeyProxy = publicKeyProxy;
    }

    public void setSignatureProxy(byte[] signatureProxy) {
        this.signatureProxy = signatureProxy;
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
