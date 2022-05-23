package Security;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public class Security {

    private static final String SHA_256_WITH_ECDSA = "SHA256withECDSA";

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
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(encodedKey);
            return keyFactory.generatePublic(encodedKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static KeyPair getKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(571);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
}
