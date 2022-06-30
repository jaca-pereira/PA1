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
    String create_account(String email);

    @POST
    @Path("/balance")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    String getBalance(String account);

    @POST
    @Path("/extract")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    String getExtract(String account);

    @POST
    @Path("/transaction")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    String sendTransaction(List<String> accounts);

    @POST
    @Path("/total_value")
    @Consumes(MediaType.APPLICATION_JSON )
    @Produces
    String getTotalValue(List<String> accounts);

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
    String mineBlock(String account);

}
