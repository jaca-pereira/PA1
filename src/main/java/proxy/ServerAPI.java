package proxy;

import data.Ledger;
import data.Transaction;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;


public interface ServerAPI {
    @GET
    @Path("/sup")
    @Produces(MediaType.APPLICATION_JSON)
    String home();
    @POST
    @Path("/account")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    byte[]  createAccount(byte[] account, byte[] signature);

    @POST
    @Path("/account/load")
    @Consumes(MediaType.APPLICATION_JSON)
    void loadMoney(byte[] account, int value, byte[] signature);

    @GET
    @Path("/account/balance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    int getBalance(byte[] account, byte[] signature);

    @GET
    @Path("/account/extract")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    List<Transaction> getExtract(byte[] account, byte[] signature);

    @POST
    @Path("/transaction")
    @Consumes(MediaType.APPLICATION_JSON)
    void sendTransaction(byte[] originAccount, byte[] destinationAccount, int value, byte[] signature, long nonce);

    @GET
    @Path("/accounts/value")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    int getTotalValue(List<byte[]> accounts, byte[] signature);

    @GET
    @Path("/value")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    int getGlobalValue(byte[] signature);

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    Ledger getLedger();
}
