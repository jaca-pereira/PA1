package test;
import client.Client;

//import org.junit.jupiter.api.TestInstance;
//import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Tests {

    private Client client;
    public Tests(Client client) {
        this.client = client;
    }

    //@Test
    public void testGeneral() {

        System.out.println(client.create_account("joao"));
        System.out.println(client.create_account("palindra"));
        System.out.println(client.create_account("rita"));
        System.out.println(client.create_account("carol"));
        System.out.println(client.mineBlock("joao")); //genesis
        System.out.println(client.mineBlock("palindra")); //reward
        List<String> accountsAndValue = new ArrayList<>(3);
        accountsAndValue.add("joao");
        accountsAndValue.add("palindra");
        accountsAndValue.add("10");
        System.out.println(client.sendTransaction(""));
        System.out.println(client.mineBlock("joao"));

        accountsAndValue = new ArrayList<>(3);
        accountsAndValue.add("joao");
        accountsAndValue.add("palindra");
        accountsAndValue.add("10");
        System.out.println(client.sendTransaction(""));
        accountsAndValue = new ArrayList<>(3);
        accountsAndValue.add("joao");
        accountsAndValue.add("rita");
        accountsAndValue.add("10");
        System.out.println(client.sendTransaction("accountsAndValue"));
        accountsAndValue = new ArrayList<>(3);
        accountsAndValue.add("joao");
        accountsAndValue.add("rita");
        accountsAndValue.add("10");
        System.out.println(client.sendTransaction("accountsAndValue"));
        System.out.println(client.mineBlock("carol"));
        System.out.println(client.mineBlock("carol"));
        System.out.println(client.getBalance("joao"));
        System.out.println(client.getBalance("rita"));
        System.out.println(client.getBalance("carol"));
        System.out.println(client.getGlobalValue());
        System.out.println(client.getExtract("carol"));

    }

}
