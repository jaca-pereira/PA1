package client;



import bftsmart.tom.util.TOMUtil;

import data.*;

import java.net.URI;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;



public class Client implements ClientAPI {
    private data.Client client;
    private KeyPair keyPair;

    public Client(URI proxyURI) throws NoSuchAlgorithmException {
       this.client = new data.Client(proxyURI);
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
