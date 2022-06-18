

import com.google.gson.Gson;

import java.io.IOException;
import java.security.KeyPair;
import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main (String[] args) throws IOException {
        Writer writer;
        try {
            writer = new Writer("requestInfo.txt");
        } catch (IOException exception) {
            writer = null;
        }
        KeyPair keyPair = Crypto.getKeyPair();
        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] privateKey = keyPair.getPrivate().getEncoded();
        List<byte[]> signatures = new LinkedList<>();
        for (LedgerRequestType requestType: LedgerRequestType.values()) {
            byte[] signature = Crypto.signRequest(keyPair.getPrivate(),requestType.toString().getBytes());
            signatures.add(signature);
        }
        Gson gson = new Gson();
        String toWrite = gson.toJson(new ToConvert(privateKey,publicKey,signatures));
        writer.writeResults(toWrite);
        writer.close();
    }
}
