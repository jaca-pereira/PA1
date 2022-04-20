package proxy;

import data.Transaction;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/")
public interface ServiceAPI {
    @POST
    @Path("/home")
    @Produces(MediaType.APPLICATION_JSON)
    String home();

    @POST
    @Path("/account")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    byte[]  createAccount(byte[] data);

    @POST
    @Path("/account/load")
    @Consumes(MediaType.APPLICATION_JSON)
    void loadMoney(byte[] data);

    @POST
    @Path("/account/balance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    int getBalance(byte[] data);

    @POST
    @Path("/account/extract")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    List<Transaction> getExtract(byte[] data);

    @POST
    @Path("/transaction")
    @Consumes(MediaType.APPLICATION_JSON)
    void sendTransaction(byte[] data);

    @POST
    @Path("/accounts/value")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    int getTotalValue(byte[] data);

    @POST
    @Path("/value")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    int getGlobalValue(byte[] data);

    @POST
    @Path("/ledger")
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, List<Transaction>> getLedger();
}
