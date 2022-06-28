package client;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
public interface ClientAPI {

    @POST
    @Path("/create_account")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    String create_account();

    @POST
    @Path("/balance")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    String getBalance();

    @POST
    @Path("/extract")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    String getExtract();

    @POST
    @Path("/transaction")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    String sendTransaction();

    @POST
    @Path("/total_value")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    String getTotalValue();

    @POST
    @Path("/global_value")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    String getGlobalValue();

    @POST
    @Path("/ledger")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    String getLedger();

    @POST
    @Path("/mine")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    String mineBlock();

}
