package client;


import bftsmart.tom.util.TOMUtil;
import com.google.gson.Gson;
import data.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.security.SecureRandom;
import java.util.List;


public class Client implements ClientAPI {
    private data.Client client;
    public Client(URI proxyURI) {
       client = new data.Client(proxyURI);
    }

    @Override
    public boolean create_account(String email) {
        String s = email.substring(10, 16);
        System.out.println(email);
        System.out.println(s);
        client.createAccount(s);
        return true;
    }

    @Override
    public boolean getBalance(String account) {
        client.getBalance(account);
        return true;
    }

    @Override
    public boolean getExtract(String account) {
        client.getExtract(account);
        return true;
    }


    @Override
    public boolean sendTransaction(List<String> accounts) {
        if (accounts.size() < 2)
            throw new WebApplicationException("Indicate origin and destination accounts!", Response.Status.BAD_REQUEST);
        client.sendTransaction(accounts.get(0), accounts.get(1));
        return true;
    }

    @Override
    public boolean getTotalValue(List<String> accounts) {
        client.getTotalValue(accounts.get(0), accounts);
        return true;
    }

    @Override
    public boolean getGlobalValue(String account) {
        client.getGlobalValue(account);
        return true;
    }

    @Override
    public boolean getLedger(String account) {
        client.getLedger(account);
        return true;
    }

    @Override
    public boolean  mineBlock(String account) {
        System.out.println("ENTROU NO MINING");
        Block blockToMine = client.getBlockToMine(account);
        boolean hasAlreadyBeenMined = false;
        int nrMiningAttempts = 0;
        SecureRandom secureRandom = new SecureRandom();
        long nonce;
        do {
            nonce = secureRandom.nextLong();
            blockToMine.setNonce(nonce);
            nrMiningAttempts++;
            if (nrMiningAttempts==5) {
                nrMiningAttempts = 0;
                hasAlreadyBeenMined = new String(client.getBlockToMine(account).getLastBlockHash()).equals(new String(blockToMine.getBlockHash()));
            }
        } while (!this.proofOfWork(blockToMine, blockToMine.getDifficulty()) && !hasAlreadyBeenMined);

        if (!hasAlreadyBeenMined)
            client.mineBlock(account, blockToMine);
        else throw new WebApplicationException("BLock Already Mined");
        return true;
    }

    private boolean proofOfWork(Block blockToMine, int difficulty) {
        try {
            byte[] blockHash = TOMUtil.computeHash(Block.serialize(blockToMine));
            int count = 0;
            for (byte b: blockHash) {
                if (b == 0) {
                    count++;
                    if (count == difficulty)
                        return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
