package client;

import data.Client;
import data.LedgerRequestType;
import data.Request;
import results.Results;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

public class ConcurrencyClient {

    private static final String PORT = "8080";

    private static Results results1;

    private static Results results2;

    private static Results results3;

    private static Results results4;

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        if (args.length < 1) {
            System.out.println("Usage: <proxy_ip_without_last_.>");
            System.exit(-1);
        }

        results1 = new Results("results_concurrent1.txt");
        results2 = new Results("results_concurrent2.txt");
        results3 = new Results("results_concurrent3.txt");
        results4 = new Results("results_concurrent4.txt");

        results1.writeResults("Concurrent Clients 1.\n");
        results1.writeResults("All durations in ms.\n");
        results2.writeResults("Concurrent Clients 2.\n");
        results2.writeResults("All durations in ms.\n");
        results3.writeResults("Concurrent Clients 3.\n");
        results3.writeResults("All durations in ms.\n");
        results4.writeResults("Concurrent Clients 4.\n");
        results4.writeResults("All durations in ms.\n");

        String originEmail = "jaca.pereira@campus.fct.unl.pt";
        String destinationEmail= "rafael.palindra@campus.fct.unl.pt";
        byte[] originAccount = NoConcurrencyClient.accountIdCreator(originEmail);
        byte[] destinationAccount = NoConcurrencyClient.accountIdCreator(destinationEmail);

        String ip = args[0] + 10;
        URI proxyURI = new URI(String.format("https://%s:%s/", ip, PORT));;
        Thread t1 = new Thread(new Runnable() {
            private Client client;
            private byte[] originAccount;

            private String originEmail;
            private Results resultsConcurrent;


            public Runnable init(Client client, byte[] originAccount, String originEmail, Results resultsConcurrent ) {
                this.client = client;
                this.originAccount = originAccount;
                this.originEmail = originEmail;
                this.resultsConcurrent = resultsConcurrent;
                return this;
            }
            @Override
            public void run() {
                try {
                    testAccountCreation(client,originAccount, originEmail, resultsConcurrent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.init(new Client(proxyURI), originAccount, originEmail, results1));


        ip = args[0] + 11;
        proxyURI = new URI(String.format("https://%s:%s/", ip, PORT));;
        Thread t2 = new Thread(new Runnable() {
            private Client client;
            private byte[] originAccount;

            private String originEmail;
            private Results resultsConcurrent;


            public Runnable init(Client client, byte[] originAccount, String originEmail, Results resultsConcurrent ) {
                this.client = client;
                this.originAccount = originAccount;
                this.originEmail = originEmail;
                this.resultsConcurrent = resultsConcurrent;
                return this;
            }
            @Override
            public void run() {
                try {
                    testAccountCreation(client,originAccount, originEmail, resultsConcurrent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.init(new Client(proxyURI), destinationAccount, destinationEmail, results2));


        ip = args[0] + 12;
        proxyURI = new URI(String.format("https://%s:%s/", ip, PORT));;
        Thread t3 = new Thread(new Runnable() {
            private Client client;
            private byte[] originAccount;
            private byte[] destinationAccount;

            private String originEmail;

            private String destinationEmail;

            private Results resultsConcurrent;

            public Runnable init(Client client, byte[] originAccount, byte[] destinationAccount, String originEmail, String destinationEmail, Results resultsConcurrent ) {
                this.client = client;
                this.originAccount = originAccount;
                this.destinationAccount = destinationAccount;
                this.originEmail = originEmail;
                this.destinationEmail = destinationEmail;
                this.resultsConcurrent = resultsConcurrent;
                return this;
            }
            @Override
            public void run() {
                try {
                    testTransactions(client,originAccount, destinationAccount, originEmail, destinationEmail, resultsConcurrent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.init(new Client(proxyURI), originAccount, destinationAccount, originEmail, destinationEmail, results3));


        ip = args[0] + 13;
        proxyURI = new URI(String.format("https://%s:%s/", ip, PORT));;
        Thread t4 = new Thread(new Runnable() {
            private Client client;
            private byte[] originAccount;
            private byte[] destinationAccount;

            private String originEmail;

            private String destinationEmail;

            private Results resultsConcurrent;

            public Runnable init(Client client, byte[] originAccount, byte[] destinationAccount, String originEmail, String destinationEmail, Results resultsConcurrent ) {
                this.client = client;
                this.originAccount = originAccount;
                this.destinationAccount = destinationAccount;
                this.originEmail = originEmail;
                this.destinationEmail = destinationEmail;
                this.resultsConcurrent = resultsConcurrent;
                return this;
            }
            @Override
            public void run() {
                try {
                    testTransactions(client,originAccount, destinationAccount, originEmail, destinationEmail, resultsConcurrent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.init(new Client(proxyURI), destinationAccount, originAccount, destinationEmail, originEmail, results4));

        t1.run();
        t2.run();
        t1.join();
        t2.join();

        t3.run();
        t4.run();
        t3.join();
        t4.join();

        results1.close();
        results2.close();
        results3.close();
        results4.close();

    }


    private static void testAccountCreation(Client client, byte[] account, String accountEmail, Results resultsConcurrent) throws IOException {

        String result="";


        //create_account
        long before = System.currentTimeMillis();
        Request request = new Request(LedgerRequestType.CREATE_ACCOUNT, account);
        result+="Creating account " + accountEmail + ".\n";
        result+=client.executeCommand(request);
        long after = System.currentTimeMillis();
        long duration = after - before;
        long totalDuration = duration;
        result += "Duration: " + String.valueOf(duration) + ".\n";
        result+="\n";

        //load_money
        before = System.currentTimeMillis();
        request = new Request(LedgerRequestType.LOAD_MONEY, account, 10);
        result+="Loading 10 money.\n";
        result+=client.executeCommand(request);
        after = System.currentTimeMillis();
        duration = after - before;
        totalDuration += duration;
        result += "Duration: " + String.valueOf(duration) + ".\n";
        result+="\n";

        //get_balance
        before = System.currentTimeMillis();
        request = new Request(LedgerRequestType.GET_BALANCE, account);
        result+="Getting account balance.\n";
        result+=client.executeCommand(request);
        after = System.currentTimeMillis();
        duration = after - before;
        totalDuration += duration;
        result += "Duration: " + String.valueOf(duration) + ".\n";
        result+="\n";

        //get_extract
        before = System.currentTimeMillis();
        request = new Request(LedgerRequestType.GET_EXTRACT, account);
        result+="Getting account extract.\n";
        result+=client.executeCommand(request);
        after = System.currentTimeMillis();
        duration = after - before;
        totalDuration += duration;
        result += "Duration: " + String.valueOf(duration) + ".\n";
        result+="\n";

        //get_global_value
        before = System.currentTimeMillis();
        request = new Request(LedgerRequestType.GET_GLOBAL_VALUE, account);
        result+="Getting global ledger value.\n";
        result+=client.executeCommand(request);
        after = System.currentTimeMillis();
        duration = after - before;
        totalDuration += duration;
        result += "Duration: " + String.valueOf(duration) + ".\n";result+="\n";


        //get_ledger
        before = System.currentTimeMillis();
        request = new Request(LedgerRequestType.GET_LEDGER);
        result+="Getting ledger.\n";
        result+=client.executeCommand(request);
        after = System.currentTimeMillis();
        duration = after - before;
        totalDuration += duration;
        result += "Duration: " + String.valueOf(duration) + ".\n";
        result+="\n";

        result+="Total duration of test: " + totalDuration +".\n";;
        resultsConcurrent.writeResults(result);
    }

    private static void testTransactions(Client client, byte[] originAccount, byte[] destinationAccount, String originAccountEmail, String destinyAccountEmail, Results resultsConcurrent) throws IOException {

        String result="";

        long before = System.currentTimeMillis();
        Request request = new Request(LedgerRequestType.SEND_TRANSACTION, originAccount, destinationAccount, 5, 1);
        result+="Sending 5 money from " + originAccountEmail + " to "+ destinyAccountEmail +"\n";
        result+=client.executeCommand(request);
        long after = System.currentTimeMillis();
        long duration = after - before;
        long totalDuration = duration;
        result += "Duration: " + String.valueOf(duration) + ".\n";
        result+="\n";

        before = System.currentTimeMillis();
        request = new Request(LedgerRequestType.GET_BALANCE, originAccount);
        result+="Getting origin account balance.\n";
        result+=client.executeCommand(request);
        after = System.currentTimeMillis();
        duration = after - before;
        totalDuration += duration;
        result += "Duration: " + String.valueOf(duration) + ".\n";
        result+="\n";


        //get_extract
        before = System.currentTimeMillis();
        request = new Request(LedgerRequestType.GET_EXTRACT, originAccount);
        result+="Getting origin account extract.\n";
        result+=client.executeCommand(request);
        after = System.currentTimeMillis();
        duration = after - before;
        totalDuration += duration;
        result += "Duration: " + String.valueOf(duration) + ".\n";
        result+="\n";

        //get_balance
        before = System.currentTimeMillis();
        request = new Request(LedgerRequestType.GET_BALANCE, destinationAccount);
        result+="Getting destination account balance.\n";
        result+= client.executeCommand(request);
        after = System.currentTimeMillis();
        duration = after - before;
        totalDuration += duration;
        result += "Duration: " + String.valueOf(duration) + ".\n";
        result+="\n";


        //get_extract
        before = System.currentTimeMillis();
        request = new Request(LedgerRequestType.GET_EXTRACT, destinationAccount);
        result+="Getting destination account extract.\n";
        result+=client.executeCommand(request);
        after = System.currentTimeMillis();
        duration = after - before;
        totalDuration += duration;
        result += "Duration: " + String.valueOf(duration) + ".\n";
        result+="\n";


        //get_total_value
        before = System.currentTimeMillis();
        List<byte[]> accounts = new LinkedList<>();
        accounts.add(originAccount);
        accounts.add(destinationAccount);
        request = new Request(LedgerRequestType.GET_TOTAL_VALUE, accounts);
        result+="Getting total accounts value.\n";
        result+=client.executeCommand(request);
        after = System.currentTimeMillis();
        duration = after - before;
        totalDuration += duration;
        result += "Duration: " + String.valueOf(duration) + ".\n";
        result+="\n";


        //get_global_value
        before = System.currentTimeMillis();
        request = new Request(LedgerRequestType.GET_GLOBAL_VALUE,originAccount);
        result+="Getting global ledger value.\n";
        result+=client.executeCommand(request);
        after = System.currentTimeMillis();
        duration = after - before;
        totalDuration += duration;
        result += "Duration: " + String.valueOf(duration) + ".\n";
        result+="\n";


        //get_ledger
        before = System.currentTimeMillis();
        request = new Request(LedgerRequestType.GET_LEDGER);
        result+="Getting ledger.\n";
        result+=client.executeCommand(request) ;
        after = System.currentTimeMillis();
        duration = after - before;
        totalDuration += duration;
        result += "Duration: " + String.valueOf(duration) + ".\n";
        result+="\n";

        result+="Total duration of test: " + totalDuration +".\n";;
        resultsConcurrent.writeResults(result);

    }
}
