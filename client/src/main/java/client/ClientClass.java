package client;

import java.io.FileInputStream;
import java.net.*;

import java.security.KeyPair;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import java.util.Scanner;


import Security.Security;

import data.Request;
import data.Transaction;
import org.glassfish.jersey.client.ClientConfig;
import data.LedgerRequestType;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import Security.InsecureHostNameVerifier;
import data.Reply;

import javax.net.ssl.TrustManagerFactory;
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

    private static final String PORT = "8080";
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: Client <first_proxy_ip> <nr_proxies>");
            System.exit(-1);
        }

        try {
            serverURI = new URI(String.format("https://%s:%s", args[0], PORT));

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
            case HOME:
                home();
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


    private static SSLContext getContext() throws Exception {
        KeyStore ts = KeyStore.getInstance("JKS");

        try (FileInputStream fis = new FileInputStream("security/clientcacerts.jks")) {
            ts.load(fis, "password".toCharArray());
        }


        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);

        String protocol = "TLSv1.2";
        SSLContext sslContext = SSLContext.getInstance(protocol);

        sslContext.init(null, tmf.getTrustManagers(), null);

        return sslContext;
    }


    private static Client startClient() {

        try {
            SSLContext context = getContext();
            HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostNameVerifier());
            HttpsURLConnection.setDefaultSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
            ClientConfig config = new ClientConfig();
            return ClientBuilder.newBuilder().sslContext(context)
                    .withConfig(config).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void home() {
        Client client = startClient();
        WebTarget target = client.target( serverURI ).path("home");
        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(null);
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                System.out.println("Success: " + r.readEntity(String.class));
            } else
                System.out.println("Error, HTTP error status: " + r.getStatus() );
        } catch ( ProcessingException pe ) { //Error in communication with server
            System.out.println("Timeout occurred.");
            pe.printStackTrace();
        }
    }

    private static void getLedger() {

        Client client = startClient();
        WebTarget target = client.target( serverURI ).path("ledger");
        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(null);
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                List<Transaction> ledger = Reply.deserialize(r.readEntity(byte[].class)).getListReply();
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

        Client client = startClient();

        KeyPair clientKeyPair = Security.getKeyPair();
        Request request = new Request(LedgerRequestType.SEND_TRANSACTION, clientKeyPair.getPublic().getEncoded(), Security.signRequest(clientKeyPair.getPrivate(), LedgerRequestType.SEND_TRANSACTION.toString().getBytes()), sender.getBytes(), reciever.getBytes(), Integer.parseInt(value), Long.parseLong(nonce));

        WebTarget target = client.target( serverURI ).path("transaction");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if(  r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                System.out.println("Success: " + Reply.deserialize(r.readEntity(byte[].class)).getBoolReply());
            } else
                System.out.println("Error, HTTP error status: " + r.getStatus() );

        } catch ( ProcessingException pe ) { //Error in communication with server
            System.out.println("Timeout occurred.");
            pe.printStackTrace(); //
        }
    }

    private static void getGlobalValue() {

        Client client = startClient();

        KeyPair clientKeyPair = Security.getKeyPair();
        Request request = new Request(LedgerRequestType.GET_GLOBAL_VALUE, clientKeyPair.getPublic().getEncoded(), Security.signRequest(clientKeyPair.getPrivate(), LedgerRequestType.GET_GLOBAL_VALUE.toString().getBytes()));

        WebTarget target = client.target( serverURI ).path("value");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                int value = Reply.deserialize(r.readEntity(byte[].class)).getIntReply();
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

        Client client = startClient();

        KeyPair clientKeyPair = Security.getKeyPair();
        Request request = new Request(LedgerRequestType.GET_TOTAL_VALUE, clientKeyPair.getPublic().getEncoded(), Security.signRequest(clientKeyPair.getPrivate(), LedgerRequestType.GET_TOTAL_VALUE.toString().getBytes()), accounts);

        WebTarget target = client.target( serverURI ).path("accounts/value");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                int value = Reply.deserialize(r.readEntity(byte[].class)).getIntReply();
                System.out.println("Success: " + value);
            } else
                System.out.println("Error, HTTP error status: " + r.getStatus() );

        } catch ( ProcessingException pe ) { //Error in communication with server
            System.out.println("Timeout occurred.");
            pe.printStackTrace(); //
        }
    }

    private static void getExtract(Scanner scanner) {
        byte[] account = scanner.nextLine().getBytes();

        Client client = startClient();

        KeyPair clientKeyPair = Security.getKeyPair();
        Request request = new Request(LedgerRequestType.GET_EXTRACT, clientKeyPair.getPublic().getEncoded(), Security.signRequest(clientKeyPair.getPrivate(), LedgerRequestType.GET_EXTRACT.toString().getBytes()), account);

        WebTarget target = client.target( serverURI ).path("account/extract");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                List<Transaction> extract = Reply.deserialize(r.readEntity(byte[].class)).getListReply();
                System.out.println("Success: " + extract);
            } else
                System.out.println("Error, HTTP error status: " + r.getStatus() );

        } catch ( ProcessingException pe ) { //Error in communication with server
            System.out.println("Timeout occurred.");
            pe.printStackTrace(); //
        }
    }

    private static void loadMoney(Scanner scanner) {
        byte[] account = scanner.nextLine().getBytes();
        String value = scanner.nextLine();
        ;

        Client client = startClient();

        KeyPair clientKeyPair = Security.getKeyPair();
        Request request = new Request(LedgerRequestType.LOAD_MONEY, clientKeyPair.getPublic().getEncoded(), Security.signRequest(clientKeyPair.getPrivate(), LedgerRequestType.LOAD_MONEY.toString().getBytes()), account, Integer.parseInt(value));

        WebTarget target = client.target( serverURI ).path("account/load");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if(  r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                System.out.println("Success: " + Reply.deserialize(r.readEntity(byte[].class)).getBoolReply());
            } else
                System.out.println("Error, HTTP error status: " + r.getStatus() );

        } catch ( ProcessingException pe ) { //Error in communication with server
            System.out.println("Timeout occurred.");
            pe.printStackTrace(); //
        }
    }

    private static void getBalance(Scanner scanner) {
        byte[] account = scanner.nextLine().getBytes();



        Client client = startClient();

        KeyPair clientKeyPair = Security.getKeyPair();
        Request request = new Request(LedgerRequestType.GET_BALANCE, clientKeyPair.getPublic().getEncoded(), Security.signRequest(clientKeyPair.getPrivate(), LedgerRequestType.GET_BALANCE.toString().getBytes()), account);

        WebTarget target = client.target( serverURI ).path("account/balance");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                int balance = Reply.deserialize(r.readEntity(byte[].class)).getIntReply();
                System.out.println("Success: " + balance);
            } else
                System.out.println("Error, HTTP error status: " + r.getStatus() );

        } catch ( ProcessingException pe ) { //Error in communication with server
            System.out.println("Timeout occurred.");
            pe.printStackTrace(); //
        }
    }

    private static void createAccount(Scanner scanner) {
        byte[] account = scanner.nextLine().getBytes();

        Client client = startClient();

        KeyPair clientKeyPair = Security.getKeyPair();
        Request request = new Request(LedgerRequestType.CREATE_ACCOUNT, clientKeyPair.getPublic().getEncoded(), Security.signRequest(clientKeyPair.getPrivate(), LedgerRequestType.CREATE_ACCOUNT.toString().getBytes()), account);

        WebTarget target = client.target( serverURI ).path("account");

        try {
            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(Request.serialize(request), MediaType.APPLICATION_JSON_TYPE));
            if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                byte[] acc = Reply.deserialize(r.readEntity(byte[].class)).getByteReply();
                System.out.println("Success: " + acc);
            } else
                System.out.println("Error, HTTP error status: " + r.getStatus());

        } catch ( ProcessingException pe ) { //Error in communication with server
            System.out.println("Timeout occurred.");
            pe.printStackTrace(); //
        }

    }


}
