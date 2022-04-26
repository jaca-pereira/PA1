package client;

import data.LedgerRequestType;
import data.Request;
import results.Results;

import java.io.IOException;
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

   private static Results results;
   public static void main(String[] args) throws URISyntaxException, IOException {
      if (args.length < 1) {
         System.out.println("Usage: <proxy_ip_without_last_.>");
         System.exit(-1);
      }


      results = new Results("results.txt");
      results.writeResults("Sequential clients. No persistency.\n");
      results.writeResults("All durations in ms.\n");
      byte[] originAccount = accountIdCreator("jaca.pereira@campus.fct.unl.pt");

      String ip = args[0] + 10;
      URI proxyURI = new URI(String.format("https://%s:%s/", ip, PORT));
      Client client = new Client(proxyURI);
      testAccountCreation(client, originAccount, "jaca.pereira@campus.fct.unl.pt");


      byte[] destinationAccount = accountIdCreator("rafael.palindra@campus.fct.unl.pt");
      ip = args[0] + 11;

      proxyURI = new URI(String.format("https://%s:%s/", ip, PORT));
      client = new Client(proxyURI);
      testAccountCreation(client,destinationAccount, "rafael.palindra@campus.fct.unl.pt");


      ip = args[0] + 12;

      proxyURI = new URI(String.format("https://%s:%s/", ip, PORT));
      client = new Client(proxyURI);
      testTransactions(client,originAccount, destinationAccount, "jaca.pereira@campus.fct.unl.pt", "rafael.palindra@campus.fct.unl.pt");


      ip = args[0] + 13;

      proxyURI = new URI(String.format("https://%s:%s/", ip, PORT));
      client = new Client(proxyURI);
      testTransactions(client, originAccount, destinationAccount, "rafael.palindra@campus.fct.unl.pt", "jaca.pereira@campus.fct.unl.pt");


      results.close();
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

   private static void testAccountCreation(Client client,byte[] account, String accountEmail) throws IOException {

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
      results.writeResults(result);
   }

   private static void testTransactions(Client client, byte[] originAccount, byte[] destinationAccount, String originAccountEmail, String destinyAccountEmail) throws IOException {

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
      results.writeResults(result);

   }


}
