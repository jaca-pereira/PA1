package client;



import bftsmart.tom.util.TOMUtil;

import data.*;
import javassist.bytecode.ByteArray;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;


public class Client implements ClientAPI {
    private data.Client client;

    public Client(URI proxyURI) throws NoSuchAlgorithmException {
       this.client = new data.Client(proxyURI);
    }

    @Override
    public boolean create_account(String email) {
        System.out.println(email);
        client.createAccount(email);
        return true;
    }

    @Override
    public int getBalance() {
        return client.getBalance();
    }

    @Override
    public List<Transaction> getExtract() {
        return client.getExtract();
    }


    @Override
    public boolean sendTransaction() {
        client.sendTransaction();
        return true;
    }

    @Override
    public int getTotalValue() {
        return client.getTotalValue();
    }

    @Override
    public int getGlobalValue() {
        return client.getGlobalValue();
    }

    @Override
    public List<Block> getLedger() {
        return client.getLedger();
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
        } while (!Block.proofOfWork(blockToMine));
        System.out.println("MINOU");
        blockToMine.setHash(TOMUtil.computeHash(Block.serialize(blockToMine)));
        client.mineBlock(blockToMine);
        return true;
    }

}
