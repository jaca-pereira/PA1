package client;


import bftsmart.tom.util.TOMUtil;
import data.*;

import javax.ws.rs.WebApplicationException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.List;


public class Client implements ClientAPI {
    private data.Client client;
    public Client(URI proxyURI) {
       client = new data.Client(proxyURI);
    }

    @Override
    public void getBalance(String account) {
        client.getBalance(account);
    }

    @Override
    public void getExtract(String account) {
        client.getExtract(account);
    }


    @Override
    public void sendTransaction(String originAccount, String destinationAccount) {
        client.sendTransaction(originAccount, destinationAccount);
    }

    @Override
    public void getTotalValue(String account, List<String> accounts) {
        client.getTotalValue(account, accounts);
    }

    @Override
    public void getGlobalValue(String account) {
        client.getGlobalValue(account);
    }

    @Override
    public void getLedger(String account) {
        client.getLedger(account);
    }

    @Override
    public void  mineBlock(String account, int difficulty) {
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
                hasAlreadyBeenMined = new String(client.getBlockToMine(account).getLastBlockHash()).equals(new String(blockToMine.thisBlockHash()));
            }
        } while (!this.proofOfWork(blockToMine, difficulty) && !hasAlreadyBeenMined);

        if (!hasAlreadyBeenMined)
            client.mineBlock(account, blockToMine);
        else throw new WebApplicationException("BLock Already Mined");
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
