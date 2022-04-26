package data;

import java.util.*;

public class LedgerDataStructure {
    private Map<String, Account> ledgerMap;
    private List<Transaction> ledgerList;

    public LedgerDataStructure() {
        this.ledgerMap = new HashMap<>();
        this.ledgerList = new LinkedList<>();
    }

    public void addAccount(byte[] account) {
        String acc = new String(account);
        if(this.ledgerMap.containsKey(acc))
            throw new IllegalArgumentException("Account already exists!");
        this.ledgerMap.put(acc, new Account());
    }

    public Account transaction(Transaction t) {
        Account account = ledgerMap.get(new String(t.getOriginAccount()));
        if (account == null)
            throw new IllegalArgumentException("Account does not exist!");
        account.addTransaction(t);
        this.ledgerList.add(t);
        return account;
    }

    public void transactionBetweenAccounts(Transaction t) {
        Account destinationAccount = ledgerMap.get(new String(t.getDestinationAccount()));
        if (destinationAccount == null)
            throw new IllegalArgumentException("Destination account does not exist!");
        if (t.getNonce()!=-1) {
            Account originAccount = ledgerMap.get(new String(t.getOriginAccount()));
            if (originAccount == null)
                throw new IllegalArgumentException("Origin account does not exist!");
            if (originAccount.getBalance()>=t.getValue()) {
                originAccount.addTransaction(t);
                originAccount.changeBalance(-t.getValue());
            }else
                throw new IllegalArgumentException("Origin account does not have sufficient balance!");
        }
        destinationAccount.addTransaction(t);
        destinationAccount.changeBalance(t.getValue());
        this.ledgerList.add(t);
    }


    public int transactionMultipleAccounts(Transaction t) {
        int totalValue = 0;
        for (byte[] account: t.getAccounts()) {
            Account acc = ledgerMap.get(new String(account));
            if (acc == null)
                throw new IllegalArgumentException("Account "+ new String(account) + " does not exist!");
            totalValue += acc.getBalance();
        }
        return totalValue;
    }

    public List<Transaction> getLedger() {
        return this.ledgerList;
    }
}