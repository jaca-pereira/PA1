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
    Reply createAccount(byte[] data);

    @POST
    @Path("/account/load")
    @Consumes(MediaType.APPLICATION_JSON)
    Reply loadMoney(byte[] data);

    @POST
    @Path("/account/balance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Reply getBalance(byte[] data);

    @POST
    @Path("/account/extract")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Reply getExtract(byte[] data);

    @POST
    @Path("/transaction")
    @Consumes(MediaType.APPLICATION_JSON)
    Reply sendTransaction(byte[] data);

    @POST
    @Path("/accounts/value")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Reply getTotalValue(byte[] data);

    @POST
    @Path("/value")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Reply getGlobalValue(byte[] data);

    @POST
    @Path("/ledger")
    @Produces(MediaType.APPLICATION_JSON)
    Reply getLedger();
}
