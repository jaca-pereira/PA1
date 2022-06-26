package client;


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
    boolean getBalance(String account);

    @POST
    @Path("/extract")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    boolean getExtract(String account);

    @POST
    @Path("/transaction")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    boolean sendTransaction(List<String> accounts);

    @POST
    @Path("/total_value")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    boolean getTotalValue(List<String> accounts);

    @POST
    @Path("/global_value")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    boolean getGlobalValue(String account);

    @POST
    @Path("/ledger")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    boolean getLedger(String account);

    @POST
    @Path("/mine")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    boolean mineBlock(String account);

}
