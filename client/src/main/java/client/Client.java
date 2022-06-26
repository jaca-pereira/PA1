package client;



import bftsmart.tom.util.TOMUtil;

import data.*;
import javassist.bytecode.ByteArray;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;



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
        } while (!Block.proofOfWork(blockToMine));
        System.out.println("MINOU");
        blockToMine.setHash(TOMUtil.computeHash(Block.serialize(blockToMine)));
        client.mineBlock(blockToMine);
        return true;
    }

}
