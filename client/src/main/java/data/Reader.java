package data;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Reader {
    private FileReader fileReaderKeys;
    private File fileKeys;
    private BufferedReader bufferedReaderKeys;

    private FileReader fileReaderURIs;
    private File fileURIs;
    private BufferedReader bufferedReaderURIs;

    public Reader(String fileNameKeys, String fileNameURIs) throws IOException {
        this.fileKeys = new File(fileNameKeys);
        if (!fileKeys.exists()) {
            throw new IOException("Keys file does not exist. Client not ready!");
        }
        this.fileReaderKeys = new FileReader(fileKeys);
        this.bufferedReaderKeys = new BufferedReader(this.fileReaderKeys);

        this.fileURIs = new File(fileNameURIs);
        if (!fileURIs.exists()) {
            throw new IOException("URIs file does not exist. Client not ready!");
        }
        this.fileReaderURIs = new FileReader(fileURIs);
        this.bufferedReaderURIs = new BufferedReader(this.fileReaderURIs);
    }

    public Keys readKeys() throws IOException {
        String keysInFile = new String();
        String read= "";
        while (!(read = this.bufferedReaderKeys.readLine()).equals("")) {
            keysInFile += read;
        }
        Gson json = new Gson();
        Keys keys = json.fromJson(keysInFile, Keys.class);
        return keys;
    }

    public List<URI> readURIs() throws IOException {
        String URIsInFile = new String();
        String read= "";
        while (!(read = this.bufferedReaderURIs.readLine()).equals("")) {
            URIsInFile += read;
        }
        Gson json = new Gson();
        List<String> uris = (LinkedList) json.fromJson(URIsInFile, List.class);
        List<URI> URIs = new ArrayList<>(uris.size());
        uris.forEach(uri -> {
            try {
                URIs.add(new URI(uri));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
        return URIs;
    }

    public void close() throws IOException {
        this.bufferedReaderKeys.close();
        this.bufferedReaderURIs.close();
    }
}
