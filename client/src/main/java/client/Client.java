package client;


import bftsmart.tom.util.TOMUtil;
import data.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;


public class Client implements ClientAPI {
    private List<data.Client> clients;
    private int currentClient;

    private Reader reader;

    public Client() throws IOException, NoSuchAlgorithmException {
        this.reader = new Reader("keys.config", "proxies.config");
        List<KeyPair> keyPairs = this.loadKeys().getKeys();
        List<URI> URIs = this.loadURIs();
        this.reader.close();
        this.clients = new ArrayList<>(keyPairs.size());
        this.establishConnections(URIs, keyPairs);
        this.currentClient = 0;
    }

    private void establishConnections(List<URI> proxyURIs, List<KeyPair> keys) throws NoSuchAlgorithmException {
        for (int i = 0; i < keys.size(); i++) {
            data.Client client = new data.Client(proxyURIs.get(i), keys.get(i));
            client.createAccount();
            this.clients.add(client);
        }
    }
    private List<URI> loadURIs() throws IOException {
        List<URI> URIs = this.reader.readURIs();
        return URIs;
    }

    private Keys loadKeys() throws IOException {
        Keys keys = this.reader.readKeys();
        return keys;
    }

    private data.Client nextClient() {
        if(++this.currentClient==5)
            this.currentClient=0;
        return this.clients.get(currentClient);
    }

    @Override
    public void getBalance() {
        data.Client client = this.nextClient();
        client.getBalance();
    }

    @Override
    public void getExtract() {
        data.Client client = this.nextClient();
        client.getExtract();
    }


    @Override
    public void sendTransaction() {
        data.Client client = this.nextClient();
        byte[] accountDestiny;
        do {
             accountDestiny = this.nextClient().getAccount();
        } while (new String(accountDestiny).equals(new String(client.getAccount())));

        client.sendTransaction(accountDestiny);
    }

    @Override
    public void getTotalValue() {
        data.Client client = this.nextClient();
        List<byte[]> accountsDestiny = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            byte[] accountDestiny;
            do {
                accountDestiny = this.nextClient().getAccount();
            } while (new String(accountDestiny).equals(new String(client.getAccount())));
        }
        client.getTotalValue(accountsDestiny);
    }

    @Override
    public void getGlobalValue() {
        data.Client client = this.nextClient();
        client.getGlobalValue();
    }

    @Override
    public void getLedger() {
        data.Client client = this.nextClient();
        client.getLedger();
    }

    @Override
    public void  mineBlock() throws Exception {
        data.Client client = this.nextClient();
        Block blockToMine = client.getBlockToMine();
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
                hasAlreadyBeenMined = new String(client.getBlockToMine().getLastBlockHash()).equals(new String(blockToMine.thisBlockHash()));
            }
        } while (!this.proofOfWork(blockToMine, 2) && !hasAlreadyBeenMined);

        if (!hasAlreadyBeenMined)
            client.mineBlock(blockToMine);
        else throw new Exception("Block Already Mined");
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
