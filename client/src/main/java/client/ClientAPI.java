package client;


import data.Block;
import data.Transaction;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public interface ClientAPI {

    @POST
    @Path("/create_account")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    boolean create_account(String email);

    @POST
    @Path("/balance")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    int getBalance();

    @POST
    @Path("/extract")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    List<Transaction> getExtract();

    @POST
    @Path("/transaction")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    boolean sendTransaction();

    @POST
    @Path("/total_value")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    int getTotalValue();

    @POST
    @Path("/global_value")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    int getGlobalValue();

    @POST
    @Path("/ledger")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    List<Block> getLedger();

    @POST
    @Path("/mine")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    boolean mineBlock();

}
