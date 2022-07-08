package proxy;


import Security.Security;
import data.*;
import java.net.URI;
import java.security.*;
import java.util.LinkedList;
import java.util.List;



public class Service implements ServiceAPI {
    private Client client;
    private KeyPair keyPair;

    public Service(URI proxyURI) throws NoSuchAlgorithmException {
        this.client = new Client(proxyURI);
        this.keyPair = Security.getKeyPair();
    }

    private byte[] sendRequest(byte[] request) {
        Request deserialized = Request.deserialize(request);
        if (Security.verifySignature(deserialized.getPublicKey(), deserialized.getRequestType().toString().getBytes(), deserialized.getSignature())) {
            return this.client.sendTransaction(request);
        } else {
            List<Reply> replicaReplies = new LinkedList<>();
            Reply reply = new Reply("Request not properly signed!");
            replicaReplies.add(reply);
            ProxyReply proxyReply = new ProxyReply();
            replicaReplies.forEach(rep -> {
                rep.setPublicKeyProxy(this.keyPair.getPublic().getEncoded());
                rep.setSignatureProxy(Security.signRequest(this.keyPair.getPrivate(), deserialized.getRequestType().toString().getBytes()));
                proxyReply.addReply(rep);
            });
            return ProxyReply.serialize(proxyReply);
        }
    }

    @Override
    public byte[] sendTransaction(byte[] request) {
        return this.sendRequest(request);
    }
}
