package client;


import javax.ws.rs.*;

@Path("/")
public interface ClientAPI {

    @POST
    @Path("/balance")
    void getBalance();

    @POST
    @Path("/extract")
    void getExtract();

    @POST
    @Path("/transaction")
    void sendTransaction();

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
    void mineBlock() throws Exception;

}
