package Security;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.X509EncodedKeySpec;

public class Security {

    public static final String SHA_256_WITH_ECDSA = "SHA256withECDSA";
    public static final String PASSWORD = "password";
    public static final String USER_KEY_ALGORITHM = "EC";
    public static final String CLIENT_CACERTS = "security/clientcacerts.jks";
    public static final String TRUSTSTORE_ALGORTIHM = "SunX509";
    public static final String PROTOCOL = "TLSv1.3";

    public static boolean verifySignature(PublicKey publicKey, byte[] request, byte[] signature) {
        try {
            Signature sig = Signature.getInstance(SHA_256_WITH_ECDSA);

            sig.initVerify(publicKey);
            sig.update(request);

            return sig.verify(signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static PublicKey getPublicKey(byte[] encodedKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(USER_KEY_ALGORITHM);
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(encodedKey);
            return keyFactory.generatePublic(encodedKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static KeyPair getKeyPair(String alias) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        FileInputStream fis = new FileInputStream("security/" + alias +"keystore.jks");
        keyStore.load(fis, PASSWORD.toCharArray());
        KeyPair keyPair = new KeyPair(keyStore.getCertificate(alias).getPublicKey(), (PrivateKey) keyStore.getKey(alias, PASSWORD.toCharArray()));
        return keyPair;
    }


    public static byte[] signRequest(PrivateKey clientPrivateKey, byte[] request) {
        try {
            Signature signature = Signature.getInstance(SHA_256_WITH_ECDSA);
            signature.initSign(clientPrivateKey);
            signature.update(request);
            return signature.sign();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static SSLContext getContext() throws Exception {
        KeyStore ts = KeyStore.getInstance("JKS");

        try (FileInputStream fis = new FileInputStream(CLIENT_CACERTS)) {
            ts.load(fis, PASSWORD.toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TRUSTSTORE_ALGORTIHM);
        tmf.init(ts);

        String protocol = PROTOCOL;
        SSLContext sslContext = SSLContext.getInstance(protocol);

        sslContext.init(null, tmf.getTrustManagers(), null);

        return sslContext;
    }

    public static KeyPair getKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        return keyPairGenerator.generateKeyPair();
    }
}
