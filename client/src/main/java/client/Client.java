package client;


import data.*;

import java.net.URI;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;


public class Client implements ClientAPI {
    private List<data.Client> clients;

    public Client(URI proxyURI) {
        List<KeyPair> keyPairs = this.loadKeys().getKeys();
        this.establishConnections(proxyURI, keyPairs);
    }

    private void establishConnections(URI proxyURI, List<KeyPair> keys) {
        this.clients = new ArrayList<>(keys.size());
        for (KeyPair keyPair: keys) {
            data.Client client = new data.Client(proxyURI, keyPair);
            this.clients.add(client);
            //client.executeCommand(new Request(LedgerRequestType.CREATE_ACCOUNT, ));
        }
    }

    private Keys loadKeys() {
        return null;
    }

    private data.Client chooseRandomClient() {
        //after choosing client, has to put him in busy line
       return null;
    }


    @Override
    public void getBalance() {
        data.Client client = this.chooseRandomClient();
        client.getBalance();
    }

    @Override
    public void getExtract() {
        data.Client client = this.chooseRandomClient();
        client.getExtract();
    }


    @Override
    public void sendTransaction() {
        data.Client client = this.chooseRandomClient();
        byte[] accountDestiny;
        do {
             accountDestiny = this.chooseRandomClient().getAccount();
        } while (new String(accountDestiny).equals(new String(client.getAccount())));

        client.sendTransaction(accountDestiny);
    }

    @Override
    public void getTotalValue() {
        data.Client client = this.chooseRandomClient();
        List<byte[]> accountsDestiny = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            byte[] accountDestiny;
            do {
                accountDestiny = this.chooseRandomClient().getAccount();
            } while (new String(accountDestiny).equals(new String(client.getAccount())));
        }

        client.getTotalValue(accountsDestiny);
    }

    @Override
    public void getGlobalValue() {
        data.Client client = this.chooseRandomClient();
        client.getGlobalValue();
    }

    @Override
    public void getLedger() {
        data.Client client = this.chooseRandomClient();
        client.getLedger();
    }

    @Override
    public void  mineBlock() {
        data.Client client = this.chooseRandomClient();
        byte[] lastMinedBlockHash = client.getLastMinedBlock();
        Block blockToMine = client.getBlockToMine();
        client.mineBlock(this.proofOfWork(lastMinedBlockHash, blockToMine));
    }

    private Block proofOfWork(byte[] lastMinedBlockHash, Block blockToMine) {
    return null;
    }

}
