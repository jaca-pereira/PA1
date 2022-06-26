package data;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Account implements Serializable {
    private List<Transaction> transactionList;
    private int balance;


    public Account() {
        this.transactionList = new LinkedList<>();
        this.balance = 0;
    }

    public List<Transaction> getTransactionList() {
        return transactionList;
    }

    public void addTransaction(Transaction transaction) {
        this.transactionList.add(transaction);
    }

    public int getBalance() {
        return this.balance;
    }

    public void changeBalance(int amount) {
        this.balance = amount;
    }

    public static byte[] serialize(Block obj) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(obj);
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Block deserialize(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return (Block) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
