package client;


import Security.Security;
import bftsmart.tom.util.TOMUtil;
import com.google.gson.Gson;
import data.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;
<<<<<<< Updated upstream
import java.security.KeyPair;
=======
import java.security.NoSuchAlgorithmException;
>>>>>>> Stashed changes
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Client implements ClientAPI {
    private data.Client client;
<<<<<<< Updated upstream
    private KeyPair keyPair;

    public Client(URI proxyURI) {
       this.client = new data.Client(proxyURI);
=======
    public Client(URI proxyURI) throws NoSuchAlgorithmException {
       client = new data.Client(proxyURI);
>>>>>>> Stashed changes
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
    public boolean getBalance() {
        client.getBalance();
        return true;
    }

    @Override
    public boolean getExtract() {
        client.getExtract();
        return true;
    }


    @Override
    public boolean sendTransaction() {
        client.sendTransaction();
        return true;
    }

    @Override
    public boolean getTotalValue() {
        client.getTotalValue();
        return true;
    }

    @Override
    public boolean getGlobalValue() {
        client.getGlobalValue();
        return true;
    }

    @Override
    public boolean getLedger() {
        client.getLedger();
        return true;
    }

    @Override
    public boolean  mineBlock() {
        System.out.println("ENTROU NO MINING");
        Block blockToMine = client.getBlockToMine();
        System.out.println("JA TEM BLOCO PARA MINAR");
        SecureRandom secureRandom = new SecureRandom();
        long nonce;
        do {
            System.out.println("MINANDO");
            nonce = secureRandom.nextLong();
            blockToMine.setNonce(nonce);
        } while (!this.proofOfWork(blockToMine, blockToMine.getDifficulty()));
        System.out.println("MINOU");
        client.mineBlock(blockToMine);
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
