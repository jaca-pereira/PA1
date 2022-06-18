package client;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
public interface ClientAPI {

    @POST
    @Path("/account")
    @Consumes(MediaType.APPLICATION_JSON)
    void createAccount(byte[] privateKey, byte[] publicKey);

    @POST
    @Path("/balance")
    void getBalance();

    @POST
    @Path("/extract")
    void getExtract();

    @POST
    @Path("/transaction")
    byte[] sendTransaction();

    @POST
    @Path("/total_value")
    void getTotalValue();

    @POST
    @Path("/global_value")
    void getGlobalValue();

    @POST
    @Path("/ledger")
    void getLedger();

    @POST
    @Path("/mine")
    void mineBlock();

}
