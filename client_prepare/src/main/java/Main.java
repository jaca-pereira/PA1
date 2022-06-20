

import com.google.gson.Gson;

import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main (String[] args) throws IOException {
        if (args.length<1) {
            System.out.println("Usage: <n_users>");
            System.exit(1);
        }
        Writer writer = null;
        try {
            writer = new Writer("keys.config");
        } catch (IOException exception) {
            System.out.println("Error opening file.");
            System.exit(1);
        }
        int nrUsers = Integer.valueOf(args[0]);
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
