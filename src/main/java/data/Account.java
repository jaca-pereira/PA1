package data;

import java.util.LinkedList;
import java.util.List;

public class Account {
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
        return balance;
    }

    public void changeBalance(int amount) {
        this.balance += amount;
    }






}
