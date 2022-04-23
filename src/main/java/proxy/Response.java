package proxy;

import data.Transaction;

import java.util.List;
import java.util.Map;

public class Response {

    //Security stuff
    private byte[] publicKey;
    private byte[] signature;

    //Response stuff
    private boolean boolResponse;
    private byte[] byteResponse;
    private int intResponse;
    private List<Transaction> listResponse;
    private Map<String, List<Transaction>> ledgerResponse;

    public Response () {}

    public Response (byte[] publicKey, byte[] signature, boolean response) {
        this.boolResponse = response;
        this.publicKey = publicKey;
        this.signature = signature;
    }
    public Response (byte[] publicKey, byte[] signature, byte[] response) {
        this.publicKey = publicKey;
        this.signature = signature;
        this.byteResponse = response;
    }

    public Response (byte[] publicKey, byte[] signature, int response) {
        this.publicKey = publicKey;
        this.signature = signature;
        this.intResponse = response;
    }

    public Response (byte[] publicKey, byte[] signature, List<Transaction> response) {
        this.publicKey = publicKey;
        this.signature = signature;
        this.listResponse = response;
    }

    public Response (byte[] publicKey, byte[] signature, Map<String, List<Transaction>> response) {
        this.publicKey = publicKey;
        this.signature = signature;
        this.ledgerResponse = response;
    }

    public boolean getBoolResponse() {
        return this.boolResponse;
    }

    public byte[] getByteResponse() {
        return this.byteResponse;
    }

    public int getIntResponse() {
        return this.intResponse;
    }

    public List<Transaction> getListResponse() {
        return this.listResponse;
    }

    public Map<String, List<Transaction>> getLedgerResponse() {
        return this.ledgerResponse;
    }

    public byte[] getPublicKey() {
        return this.publicKey;
    }

    public byte[] getSignature() {
        return this.signature;
    }
}
