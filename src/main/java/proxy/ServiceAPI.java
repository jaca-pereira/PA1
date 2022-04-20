package proxy;

import data.Ledger;
import data.Transaction;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/")
public interface ServiceAPI {
    @GET
    @Path("/home")
    @Produces(MediaType.APPLICATION_JSON)
    String home();

    @POST
    @Path("/account")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    byte[]  createAccount(Data data);

    @POST
    @Path("/account/load")
    @Consumes(MediaType.APPLICATION_JSON)
    void loadMoney(Data data);

    @GET
    @Path("/account/balance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    int getBalance(Data data);

    @GET
    @Path("/account/extract")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    List<Transaction> getExtract(Data data);

    @POST
    @Path("/transaction")
    @Consumes(MediaType.APPLICATION_JSON)
    boolean sendTransaction(Data data);

    @GET
    @Path("/accounts/value")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    int getTotalValue(Data data);

    @GET
    @Path("/value")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    int getGlobalValue(Data data);

    @GET
    @Path("/ledger")
    @Produces(MediaType.APPLICATION_JSON)
    String getLedger();
}
