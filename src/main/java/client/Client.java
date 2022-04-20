package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import proxy.Data;
import proxy.LedgerRequestType;
public class Client {
    public static final String END = "END";
    private static String serverURL;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: Client <ip> <port>");
            System.exit(-1);
        }
        serverURL = String.format("https://%s:%s/", args[0], args[1]);

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

    private static void postWithoutProduces(Data data, String endpoint) {
        try {
            URL url = new URL(serverURL.concat(endpoint));
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            byte[] input = mapper.writeValueAsString(data).getBytes();
            os.write(input, 0, input.length);

        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void post(Data data, String endpoint) {
        try {
            URL url = new URL(serverURL.concat(endpoint));
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            byte[] input = mapper.writeValueAsString(data).getBytes();
            os.write(input, 0, input.length);

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void get(Data data, String endpoint) {
        try {
            URL url = new URL(serverURL.concat(endpoint));
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            byte[] input = mapper.writeValueAsString(data).getBytes();
            os.write(input, 0, input.length);

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void getWithoutConsumes(String endpoint) {
        try {
            URL url = new URL(serverURL.concat(endpoint));
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            byte[] input = new byte[]{};
            os.write(input, 0, input.length);

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void getLedger() {
        getWithoutConsumes("ledger");
    }

    private static void sendTransaction(Scanner scanner) {
        String sender = scanner.nextLine();
        String reciever = scanner.nextLine();
        String value = scanner.nextLine();
        String nonce = scanner.nextLine();
        Data data = new Data(new byte[] {}, sender.getBytes(), reciever.getBytes(), Integer.parseInt(value), Long.parseLong(nonce));
        postWithoutProduces(data, "/transaction");
    }

    private static void getGlobalValue() {
        Data data = new Data (new byte[] {});
        get(data, "/value");
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
        get(data, "/accounts/value");
    }

    private static void getExtract(Scanner scanner) {
        String account = scanner.nextLine();

        Data data = new Data(new byte[] {}, account.getBytes());
        get(data, "/account/extract");
    }

    private static void loadMoney(Scanner scanner) {
        String account = scanner.nextLine();
        String value = scanner.nextLine();

        Data data = new Data(new byte[] {}, account.getBytes(), Integer.parseInt(value));
        postWithoutProduces(data, "/account/load");
    }

    private static void getBalance(Scanner scanner) {
        String account = scanner.nextLine();

        Data data = new Data(new byte[] {}, account.getBytes());
        get(data, "account/balance");
    }

    private static void createAccount(Scanner scanner) {
        String account = scanner.nextLine();
        byte[] signature = new byte[]{};
        Data data = new Data(account.getBytes(), signature);
        post(data, "/account");
    }


}
