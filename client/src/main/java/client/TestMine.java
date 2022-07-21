package client;

import client.Client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import java.net.URI;
import java.security.NoSuchAlgorithmException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestMine {

    private Client client;
    public TestMine(Client client) {
        this.client = client;
    }

    @Test
    public void test() {

        System.out.println(client.create_account("\"joao\""));
        System.out.println(client.create_account("\"palindra\""));
        System.out.println(client.create_account("\"rita\""));
        System.out.println(client.create_account("\"carolina\""));
        System.out.println(client.mineBlock("\"joao\""));
        System.out.println(client.mineBlock("\"palindra\""));
        System.out.println(client.sendTransaction("\"joao rita 1\""));
        client.mineBlock("\"rita\"");
        System.out.println(client.sendTransaction("\"joao rita 1\""));
        System.out.println(client.sendTransaction("\"joao rita 1\""));
        System.out.println(client.sendTransaction("\"joao rita 1\""));
        System.out.println(client.sendTransaction("\"joao rita 1\""));
        System.out.println(client.sendTransaction("\"joao rita 1\""));
        System.out.println(client.sendTransaction("\"joao rita 1\""));
        long begin = System.currentTimeMillis();
        client.mineBlock("\"rita\"");
        long end = System.currentTimeMillis();
        long totalTime = end - begin;
        System.out.println(totalTime);
    }

}