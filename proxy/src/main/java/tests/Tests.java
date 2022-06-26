package tests;

import Security.Security;
import data.LedgerRequestType;
import data.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import proxy.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.cert.CertificateException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Tests {

    Service service;
    KeyPair keyPair;
    Tests() throws NoSuchAlgorithmException {
        service= new Service(10, "server", true);
        keyPair = this.getKeyPair();
    }

    private KeyPair getKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPair = KeyPairGenerator.getInstance("EC");
        return keyPair.generateKeyPair();
    }
    private byte[] idMaker(String email) throws NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, IOException, KeyStoreException {
        SecureRandom generator = new SecureRandom();
        byte[] srn = generator.generateSeed(32);

        byte[] timer = String.valueOf(System.currentTimeMillis()).getBytes();
        byte[] emailBytes = email.getBytes();
        ByteBuffer buffersha256 = ByteBuffer.wrap(new byte[emailBytes.length + srn.length + timer.length]);
        buffersha256.put(emailBytes);
        buffersha256.put(srn);
        buffersha256.put(timer);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] sha256 = digest.digest(buffersha256.array());

        byte[] publicKey = keyPair.getPublic().getEncoded();
        ByteBuffer buffer = ByteBuffer.wrap(new byte[sha256.length + publicKey.length]);
        buffer.put(sha256);
        buffer.put(publicKey);

        return buffer.array();
    }

    @Test
    void shouldDetectInvalidAmountAtSpawn() throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        byte[] account = this.idMaker("user_0_");
        Request request = new Request(LedgerRequestType.CREATE_ACCOUNT, account);
        request.setPublicKey(keyPair.getPublic().getEncoded());
        request.setSignature(Security.signRequest(keyPair.getPrivate(), request.getRequestType().toString().getBytes()));
        service.createAccount(Request.serialize(request));
    }
}

