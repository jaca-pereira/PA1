package client;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


import data.Transaction;

import proxy.Data;
import proxy.LedgerRequestType;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ClientClass {
    public static final String END = "END";
    private static URI serverURI;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: Client <ip> <port>");
            System.exit(-1);
        }

        try {
            serverURI = new URI(String.format("http://%s:%s/", args[0], args[1]));
        } catch (Exception e) {
            System.exit(-1);
        }


        Scanner scanner = new Scanner(System.in);
        String command = null;
        do {
            command = scanner.nextLine().toUpperCase();
            executeCommand(LedgerRequestType.valueOf(command), scanner);
        }
        while(!command.equals(END));
        scanner.close();
    }

    private static void executeCommand(LedgerRequestType command, Scanner scanner) {
        switch (command) {
            case CREATE_ACCOUNT:
                createAccount(scanner);
                break;
            case GET_BALANCE:
                getBalance(scanner);
                break;
            case LOAD_MONEY:
                loadMoney(scanner);
                break;
            case GET_EXTRACT:
                getExtract(scanner);
                break;
            case GET_TOTAL_VALUE:
                getTotalValue(scanner);
                break;
            case GET_GLOBAL_VALUE:
                getGlobalValue();
                break;
            case SEND_TRANSACTION:
                sendTransaction(scanner);
                break;
            case GET_LEDGER:
                getLedger();
                break;
            default:
                System.out.println("Command unknown!");
        }
    }



    private static void getLedger() {

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target( serverURI ).path("ledger");
        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(null);
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                Map<byte[], List<Transaction>> ledger = r.readEntity(Map.class);
                System.out.println("Success: " + ledger);
            } else
                System.out.println("Error, HTTP error status: " + r.getStatus() );
        } catch ( ProcessingException pe ) { //Error in communication with server
            System.out.println("Timeout occurred.");
            pe.printStackTrace();
        }

    }

    private static void sendTransaction(Scanner scanner) {
        String sender = scanner.nextLine();
        String reciever = scanner.nextLine();
        String value = scanner.nextLine();
        String nonce = scanner.nextLine();
        Data data = new Data(new byte[] {}, sender.getBytes(), reciever.getBytes(), Integer.parseInt(value), Long.parseLong(nonce));

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target( serverURI ).path("transaction");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Data.serialize(data), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode()) {
                System.out.println("Success");
            } else
                System.out.println("Error, HTTP error status: " + r.getStatus() );

        } catch ( ProcessingException pe ) { //Error in communication with server
            System.out.println("Timeout occurred.");
            pe.printStackTrace(); //
        }
    }

    private static void getGlobalValue() {
        Data data = new Data (new byte[] {});
        Client client = ClientBuilder.newClient();

        WebTarget target = client.target( serverURI ).path("value");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Data.serialize(data), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                int value = r.readEntity(int.class);
                System.out.println("Success: " + value);
            } else
                System.out.println("Error, HTTP error status: " + r.getStatus() );

        } catch ( ProcessingException pe ) { //Error in communication with server
            System.out.println("Timeout occurred.");
            pe.printStackTrace(); //
        }
    }

    private static void getTotalValue(Scanner scanner) {
        List<byte[]> accounts = new ArrayList<>();
        while (true) {
            String account = scanner.nextLine();
            if (account.equals(""))
                break;
            accounts.add(account.getBytes());
        }

        Data data = new Data(new byte[] {}, accounts);

        Client client = ClientBuilder.newClient();

        WebTarget target = client.target( serverURI ).path("accounts/value");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Data.serialize(data), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                int value = r.readEntity(int.class);
                System.out.println("Success: " + value);
            } else
                System.out.println("Error, HTTP error status: " + r.getStatus() );

        } catch ( ProcessingException pe ) { //Error in communication with server
            System.out.println("Timeout occurred.");
            pe.printStackTrace(); //
        }
    }

    private static void getExtract(Scanner scanner) {
        String account = scanner.nextLine();

        Data data = new Data(new byte[] {}, account.getBytes());
        Client client = ClientBuilder.newClient();

        WebTarget target = client.target( serverURI ).path("account/extract");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Data.serialize(data), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                List<Transaction> extract = r.readEntity(List.class);
                System.out.println("Success: " + extract);
            } else
                System.out.println("Error, HTTP error status: " + r.getStatus() );

        } catch ( ProcessingException pe ) { //Error in communication with server
            System.out.println("Timeout occurred.");
            pe.printStackTrace(); //
        }
    }

    private static void loadMoney(Scanner scanner) {
        String account = scanner.nextLine();
        String value = scanner.nextLine();

        Data data = new Data(new byte[] {}, account.getBytes(), Integer.parseInt(value));

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target( serverURI ).path("account/load");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Data.serialize(data), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode()) {
                System.out.println("Success");
            } else
                System.out.println("Error, HTTP error status: " + r.getStatus() );

        } catch ( ProcessingException pe ) { //Error in communication with server
            System.out.println("Timeout occurred.");
            pe.printStackTrace(); //
        }
    }

    private static void getBalance(Scanner scanner) {
        String account = scanner.nextLine();

        Data data = new Data(new byte[]{}, account.getBytes());

        Client client = ClientBuilder.newClient();

        WebTarget target = client.target( serverURI ).path("account/balance");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Data.serialize(data), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                int balance = r.readEntity(int.class);
                System.out.println("Success: " + balance);
            } else
                System.out.println("Error, HTTP error status: " + r.getStatus() );

        } catch ( ProcessingException pe ) { //Error in communication with server
            System.out.println("Timeout occurred.");
            pe.printStackTrace(); //
        }
    }

    private static void createAccount(Scanner scanner) {
        String account = scanner.nextLine();
        Data data = new Data(new byte[]{}, account.getBytes());
        Client client = ClientBuilder.newClient();

        WebTarget target = client.target( serverURI ).path("account");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Data.serialize(data), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                byte[] acc = r.readEntity(byte[].class);
                System.out.println("Success: " + acc);
            } else
                System.out.println("Error, HTTP error status: " + r.getStatus());

        } catch ( ProcessingException pe ) { //Error in communication with server
            System.out.println("Timeout occurred.");
            pe.printStackTrace(); //
        }

    }


}
