package client;

import data.Client;
import data.LedgerRequestType;
import data.Request;
import results.Results;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

public class ClientAfterReplicaFail {
    private static final String PORT = "8080";


    private static Results results;
    public static void main(String[] args) throws URISyntaxException, IOException, NoSuchAlgorithmException {
        if (args.length < 1) {
            System.out.println("Usage: <proxy_ip_without_last_.>");
            System.exit(-1);
        }


        results = new Results("results_replica_fail.txt");
        results.writeResults("Sequential Client.\n");
        results.writeResults("All durations in ms.\n");


        String ip = args[0] + 10;
        URI proxyURI = new URI(String.format("https://%s:%s/", ip, PORT));
        Client client = new Client(proxyURI);
        
        testGetLedger(client, results);

        results.close();
    }

    private static void testGetLedger(Client client, Results results) throws NoSuchAlgorithmException, IOException {

        String result = "";
        long before = System.currentTimeMillis();
        Request request = new Request(LedgerRequestType.GET_LEDGER);
        result+="Getting ledger.\n";
        result+=client.executeCommand(request);
        long after = System.currentTimeMillis();
        long duration = after - before;  result += "Duration: " + String.valueOf(duration) + ".\n";
        results.writeResults(result);
    }
}
