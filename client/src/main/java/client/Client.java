package client;



import bftsmart.tom.util.TOMUtil;

import data.*;
import javassist.bytecode.ByteArray;

import javax.ws.rs.WebApplicationException;
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
    public String create_account() {
        try {
            client.createAccount();
            return "true";
        } catch (WebApplicationException e) {
            return e.getMessage();
        }

    }

    @Override
    public String getBalance() {
        try {
            return String.valueOf(client.getBalance());
        }catch (WebApplicationException e) {
            return e.getMessage();
        }
    }

    @Override
    public String getExtract() {
        try {
            String result = "";
            List<Transaction> extract = client.getExtract();
            for (Transaction t: extract) {
                result+= t.toString();
            }
            return result;
        } catch (WebApplicationException e) {
            return e.getMessage();
        }
    }


    @Override
    public String sendTransaction() {
        try {
            client.sendTransaction();
            return "true";
        } catch (WebApplicationException e) {
            return e.getMessage();
        }
    }

    @Override
    public String getTotalValue() {
        try {
            return String.valueOf(client.getTotalValue());
        } catch (WebApplicationException e) {
            return e.getMessage();
        }
    }

    @Override
    public String getGlobalValue() {
        try {
            return String.valueOf(client.getGlobalValue());
        } catch (WebApplicationException e) {
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
            return e.getMessage();
        }
    }

    @Override
    public String  mineBlock() {
        System.out.println("ENTROU NO MINING");
        try {
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
            return "true";
        } catch (WebApplicationException e) {
            return e.getMessage();
        }
    }

}
