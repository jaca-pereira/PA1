package test;
import client.Client;

import data.Block;
import data.Transaction;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.List;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Tests {

    private Client client;
    public Tests() throws NoSuchAlgorithmException {
        client = new Client(URI.create("https://172.19.10.0:8080/"));
    }

    @Test
    public void testGeneral() {

        System.out.println(client.create_account("joao"));
        System.out.println(client.create_account("manel"));
        System.out.println(client.create_account("toino"));
        System.out.println(client.create_account("ze palindra"));
        System.out.println(client.mineBlock());
        System.out.println(client.mineBlock());
        System.out.println(client.mineBlock());
        System.out.println(client.sendTransaction(0));
        System.out.println(client.sendTransaction(10));
        System.out.println(client.sendTransaction(20));
        System.out.println(client.sendTransaction(1000));
        System.out.println();
        System.out.println(client.getLedger());
        System.out.println();
        System.out.println(client.getExtract());
        System.out.println();
        System.out.println(client.getBalance());
        System.out.println();
        System.out.println(client.getGlobalValue());
        System.out.println();
        System.out.println(client.getTotalValue());
    }

}
