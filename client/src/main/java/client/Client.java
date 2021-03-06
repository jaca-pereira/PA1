package client;



import bftsmart.tom.util.TOMUtil;

import data.*;

import javax.ws.rs.WebApplicationException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;


public class Client implements ClientAPI {
    private data.Client client;

    public Client(String proxyURI, boolean sgx) throws NoSuchAlgorithmException {
       this.client = new data.Client(proxyURI, sgx);

    }
    public Client(String proxyURI, boolean sgx, String sgxURI) throws NoSuchAlgorithmException {
       this.client = new data.Client(proxyURI, sgx, sgxURI);

    }

    @Override
    public String create_account(String email) {
        try {
            email = email.substring(1,email.length()-1);
            client.createAccount(email);
            return "true";
        } catch (WebApplicationException e) {
            e.printStackTrace();
            return e.getMessage();
        }

    }

    @Override
    public String getBalance(String account) {
        try {
            account = account.substring(1,account.length()-1);
            return String.valueOf(client.getBalance(account));
        }catch (WebApplicationException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    public String getExtract(String account) {
        try {
            String result = "";
            account = account.substring(1,account.length()-1);
            List<Transaction> extract = client.getExtract(account);
            for (Transaction t: extract) {
                result+= t.toString();
            }
            return result;
        } catch (WebApplicationException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    @Override
    public String sendTransaction(String accountAndValue) {
        try {

            accountAndValue = accountAndValue.substring(1,accountAndValue.length()-1);
            String[] list = accountAndValue.split(" ");
            client.sendTransaction(list[0], list[1], Integer.valueOf(list[2]));
            return "true";
        } catch (WebApplicationException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    public String getTotalValue(List<String> accounts) {
        try {
            accounts.forEach(account -> account = account.substring(1,account.length()-1));
            return String.valueOf(client.getTotalValue(accounts));
        } catch (WebApplicationException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    public String getGlobalValue() {
        try {
            return String.valueOf(client.getGlobalValue());
        } catch (WebApplicationException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    public String getLedger() {
        try {
            String result = "";
            List<Block> ledger = client.getLedger();
            for (Block b: ledger) {
                result+= b.toString();
            }
            return result;
        } catch (WebApplicationException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    public String  mineBlock(String account) {
        try {

            account = account.substring(1,account.length()-1);
            Block blockToMine = client.getBlockToMine(account);
            if (blockToMine == null)
                return "false";
            SecureRandom secureRandom = new SecureRandom();
            long nonce;
            do {
                nonce = secureRandom.nextLong();
                blockToMine.setNonce(nonce);
            } while (!Block.proofOfWork(blockToMine));
            blockToMine.setHash(TOMUtil.computeHash(BlockHeader.serialize(blockToMine.getHeader())));
            client.mineBlock(blockToMine);
            System.out.println(account + " minou");
            return "true";
        } catch (WebApplicationException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

}
