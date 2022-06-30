package client;



import bftsmart.tom.util.TOMUtil;

import data.*;
import test.Tests;

import javax.ws.rs.WebApplicationException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;


public class Client implements ClientAPI {
    private data.Client client;

    private boolean artillery;

    public Client(URI proxyURI, boolean artillery) throws NoSuchAlgorithmException {
       this.client = new data.Client(proxyURI);
       this.artillery = artillery;
       if (!this.artillery) {
           Tests tests = new Tests(this);
           tests.testGeneral();
       }
    }

    @Override
    public String create_account(String email) {
        try {
            if (artillery)
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
            if (artillery)
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
            if (artillery)
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
