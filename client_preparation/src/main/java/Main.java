

import com.google.gson.Gson;

import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main (String[] args) throws IOException {
        Writer writer;
        try {
            writer = new Writer("keys.txt");
        } catch (IOException exception) {
            writer = null;
        }
        int nrUsers = 0;
        List<KeyPair> keys = new ArrayList<>(nrUsers);
        for (int i = 0; i <nrUsers; i++) {
            KeyPair keyPair = Crypto.getKeyPair();
            keys.add(keyPair);
        }
        Gson gson = new Gson();
        String toWrite = gson.toJson(new Keys(keys));
        writer.writeResults(toWrite);
        writer.close();
    }
}
