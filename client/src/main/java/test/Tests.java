package test;
import client.Client;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.NoSuchAlgorithmException;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Tests {

    private Client client;
    public Tests(Client client) {
        this.client = client;
    }

    @Test
    public void testGeneral() {

        System.out.println(client.create_account());
        System.out.println(client.create_account());
        System.out.println(client.create_account());
        System.out.println(client.create_account());
        System.out.println(client.mineBlock());
        System.out.println(client.mineBlock());
        System.out.println(client.sendTransaction());
        System.out.println(client.sendTransaction());
        System.out.println(client.sendTransaction());
        System.out.println(client.sendTransaction());
        System.out.println(client.sendTransaction());
        System.out.println(client.sendTransaction());
        System.out.println(client.sendTransaction());
        System.out.println(client.sendTransaction());
        long beforeTime = System.currentTimeMillis();
        System.out.println(client.mineBlock());
        long time = System.currentTimeMillis() - beforeTime;
        System.out.println("DURAÇÃO MINING :" + time);
        beforeTime = System.currentTimeMillis();
        System.out.println(client.mineBlock());
        time = System.currentTimeMillis() - beforeTime;
        System.out.println("DURAÇÃO MINING :" + time);
    }

}
