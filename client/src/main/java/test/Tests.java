package test;
import client.Client;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.NoSuchAlgorithmException;


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
        assert client.mineBlock();
        assert client.mineBlock();
        assert client.sendTransaction();
    }

    @Test
    public void mineBlock() {

    }
}
