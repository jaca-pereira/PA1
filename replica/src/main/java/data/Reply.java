package data;

import Security.Security;

import java.io.*;
import java.security.PublicKey;
import java.util.List;

public class Reply implements Serializable {

    private static final long serialVersionUID = 1L;


    private LedgerDataStructure ledgerReply;
    //Security stuff
    private byte[] publicKeyProxy;
    private byte[] signatureProxy;

    private byte[] publicKeyReplica;
    private byte[] signatureReplica;
    //Response stuff
    private boolean boolReply;
    private byte[] byteReply;
    private int intReply;
    private List<Transaction> listReply;

    private LedgerRequestType requestType;

    private Block blockReply;

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

    public Reply(List<Transaction> listReply, LedgerRequestType requestType) {
        this.publicKeyProxy = null;
        this.signatureProxy = null;
        this.publicKeyReplica = null;
        this.signatureReplica = null;
        this.listReply = listReply;
    }
    public Reply(Block blockReply, LedgerRequestType requestType) {
        this.publicKeyProxy = null;
        this.signatureProxy = null;
        this.publicKeyReplica = null;
        this.signatureReplica = null;
        this.blockReply = blockReply;
        this.requestType = requestType;
    }

    public Reply(LedgerDataStructure ledgerReply, LedgerRequestType requestType) {
        this.publicKeyProxy = null;
        this.signatureProxy = null;
        this.publicKeyReplica = null;
        this.signatureReplica = null;
        this.ledgerReply = ledgerReply;
        this.requestType = requestType;
    }

    public Block getBlockReply() {
        return this.blockReply;
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


    public PublicKey getPublicKeyProxy() {
        return Security.getPublicKey(this.publicKeyProxy);
    }

    public byte[] getSignatureProxy() {
        return signatureProxy;
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

    public LedgerRequestType getRequestType() {
        return this.requestType;
    }

    public PublicKey getPublicKeyReplica() {
        return Security.getPublicKey(this.publicKeyReplica);
    }

    public byte[] getSignatureReplica() {
        return this.signatureReplica;
    }

    public void setPublicKeyReplica(byte[] publicKeyReplica) {
        this.publicKeyReplica = publicKeyReplica;
    }

    public void setSignatureReplica(byte[] signatureReplica) {
        this.signatureReplica = signatureReplica;
    }

    public LedgerDataStructure getLedgerReply() {
        return ledgerReply;
    }

}
