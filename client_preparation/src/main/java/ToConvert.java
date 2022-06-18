import java.util.List;

public class ToConvert {

    private byte[] privateKey;
    private byte[] publicKey;

    private List<byte[]> signatures;

    public ToConvert(byte[] privateKey, byte[] publicKey, List<byte[]> signatures) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.signatures = signatures;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public List<byte[]> getSignatures() {
        return signatures;
    }

}
