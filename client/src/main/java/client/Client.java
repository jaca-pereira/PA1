package client;


import data.*;

import java.security.KeyPair;


public class Client implements ClientAPI {

    private Keys keys;

    public Client() {
        this.keys = this.loadKeys();
    }

    private Keys loadKeys() {
        return null;
    }

    private KeyPair chooseRandomKey() {
        return null;
    }


    @Override
    public void createAccount(byte[] privateKey, byte[] publicKey) {

    }

    @Override
    public void getBalance() {
        KeyPair keyPair = this.chooseRandomKey();
        //mandar pdido de getBalance para o proxy
    }

    @Override
    public void getExtract() {

    }

    @Override
    public byte[] sendTransaction() {
        return new byte[0];
    }

    @Override
    public void getTotalValue() {

    }

    @Override
    public void getGlobalValue() {

    }

    @Override
    public void getLedger() {

    }

    @Override
    public void  mineBlock() {

    }

}
