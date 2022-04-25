package client;

import data.LedgerRequestType;
import data.Request;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class OperationsTestClient {

   private static final String PORT = "8080";
   public static void main(String[] args) throws URISyntaxException {
      if (args.length < 1) {
         System.out.println("Usage: <proxy_ip_without_last_.>");
         System.exit(-1);
      }

      byte[] originAccount = accountIdCreator("jaca.pereira@campus.fct.unl.pt");

      String ip = args[0] + 10;
      System.out.println(ip);
      URI proxyURI = new URI(String.format("https://%s:%s/", ip, PORT));
      Client client = new Client(proxyURI);
      testAccountCreation(client, originAccount, "jaca.pereira@campus.fct.unl.pt");
      System.out.println();

      byte[] destinationAccount = accountIdCreator("rafael.palindra@campus.fct.unl.pt");
      ip = args[0] + 11;
      System.out.println(ip);
      proxyURI = new URI(String.format("https://%s:%s/", ip, PORT));
      client = new Client(proxyURI);
      testAccountCreation(client,destinationAccount, "rafael.palindra@campus.fct.unl.pt");
      System.out.println();

      ip = args[0] + 12;
      System.out.println(ip);
      proxyURI = new URI(String.format("https://%s:%s/", ip, PORT));
      client = new Client(proxyURI);
      testTransactions(client,originAccount, destinationAccount, "jaca.pereira@campus.fct.unl.pt", "rafael.palindra@campus.fct.unl.pt");
      System.out.println();

      ip = args[0] + 13;
      System.out.println(ip);
      proxyURI = new URI(String.format("https://%s:%s/", ip, PORT));
      client = new Client(proxyURI);
      testTransactions(client, originAccount, destinationAccount, "rafael.palindra@campus.fct.unl.pt", "jaca.pereira@campus.fct.unl.pt");
      System.out.println();

      System.out.println("Test Ended.");
   }
   private static byte[] accountIdCreator(String accountEmail) {
      byte[] email = accountEmail.getBytes();

      SecureRandom generator = new SecureRandom();
      byte[] srn = generator.generateSeed(32);

      byte[] timer = String.valueOf(System.currentTimeMillis()).getBytes();

      ByteBuffer accountId = ByteBuffer.wrap(new byte[email.length + srn.length + timer.length]);
      accountId.put(email);
      accountId.put(srn);
      accountId.put(timer);

      return accountId.array();
   }

   private static void testAccountCreation(Client client,byte[] account, String accountEmail) {


      //create_account
      Request request = new Request(LedgerRequestType.CREATE_ACCOUNT, account);
      System.out.println("Creating account " + accountEmail + ".");
      System.out.println(client.executeCommand(request));

      //load_money
      request = new Request(LedgerRequestType.LOAD_MONEY, account, 10);
      System.out.println("Loading 10 money.");
      System.out.println(client.executeCommand(request));

      request = new Request(LedgerRequestType.GET_BALANCE, account);
      System.out.println("Getting account balance.");
      System.out.println(client.executeCommand(request));

      //get_extract
      request = new Request(LedgerRequestType.GET_EXTRACT, account);
      System.out.println("Getting account extract.");
      System.out.println(client.executeCommand(request));

      //get_global_value
      request = new Request(LedgerRequestType.GET_GLOBAL_VALUE, account);
      System.out.println("Getting global ledger value.");
      System.out.println(client.executeCommand(request));

      //get_ledger
      request = new Request(LedgerRequestType.GET_LEDGER);
      System.out.println("Getting ledger.");
      System.out.println(client.executeCommand(request));


   }

   private static void testTransactions(Client client, byte[] originAccount, byte[] destinationAccount, String originAccountEmail, String destinyAccountEmail) {


      Request request = new Request(LedgerRequestType.SEND_TRANSACTION, originAccount, destinationAccount, 5, 1);
      System.out.println("Sending 5 money from " + originAccountEmail + " to "+ destinyAccountEmail);
      System.out.println(client.executeCommand(request));

      request = new Request(LedgerRequestType.GET_BALANCE, originAccount);
      System.out.println("Getting origin account balance.");
      System.out.println(client.executeCommand(request));

      //get_extract
      request = new Request(LedgerRequestType.GET_EXTRACT, originAccount);
      System.out.println("Getting origin account extract.");
      System.out.println(client.executeCommand(request));

      request = new Request(LedgerRequestType.GET_BALANCE, destinationAccount);
      System.out.println("Getting destination account balance.");
      System.out.println(client.executeCommand(request));

      //get_extract
      request = new Request(LedgerRequestType.GET_EXTRACT, destinationAccount);
      System.out.println("Getting destination account extract.");
      System.out.println(client.executeCommand(request));

      //get_total_value
      List<byte[]> accounts = new LinkedList<>();
      accounts.add(originAccount);
      accounts.add(destinationAccount);
      request = new Request(LedgerRequestType.GET_TOTAL_VALUE, accounts);
      System.out.println("Getting total accounts value.");
      System.out.println(client.executeCommand(request));

      //get_global_value
      request = new Request(LedgerRequestType.GET_GLOBAL_VALUE,originAccount);
      System.out.println("Getting global ledger value.");
      System.out.println(client.executeCommand(request));

      //get_ledger
      request = new Request(LedgerRequestType.GET_LEDGER);
      System.out.println("Getting ledger.");
      System.out.println(client.executeCommand(request));

   }


}
