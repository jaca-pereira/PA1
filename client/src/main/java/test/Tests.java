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

        assert client.create_account("joao");
        assert client.create_account("manel");
        assert client.create_account("toino");
        assert client.create_account("ze palindra");
        assert client.mineBlock();
        assert client.mineBlock();
        assert client.sendTransaction();
        assert client.mineBlock();
        assert client.sendTransaction();
        assert client.sendTransaction();
        assert client.sendTransaction();
        List<Transaction> transactions =client.getExtract();
        for (Transaction t: transactions) {
            System.out.println(t.toString());
            System.out.println();
        }
        System.out.println(client.getBalance());
        System.out.println();
        System.out.println(client.getGlobalValue());
        System.out.println();
        System.out.println(client.getTotalValue());
    }

}
