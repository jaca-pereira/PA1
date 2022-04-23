package proxy;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

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
    byte[] createAccount(byte[] data);

    @POST
    @Path("/account/load")
    @Consumes(MediaType.APPLICATION_JSON)
    byte[] loadMoney(byte[] data);

    @POST
    @Path("/account/balance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    byte[] getBalance(byte[] data);

    @POST
    @Path("/account/extract")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    byte[] getExtract(byte[] data);

    @POST
    @Path("/transaction")
    @Consumes(MediaType.APPLICATION_JSON)
    byte[] sendTransaction(byte[] data);

    @POST
    @Path("/accounts/value")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    byte[] getTotalValue(byte[] data);

    @POST
    @Path("/value")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    byte[] getGlobalValue(byte[] data);

    @POST
    @Path("/ledger")
    @Produces(MediaType.APPLICATION_JSON)
    byte[] getLedger();
}
