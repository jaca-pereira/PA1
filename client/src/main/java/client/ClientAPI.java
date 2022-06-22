package client;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public interface ClientAPI {

    @POST
    @Path("/create_account")
    @Consumes(MediaType.APPLICATION_JSON )
    void create_account(String email);

    @POST
    @Path("/balance")
    @Consumes(MediaType.APPLICATION_JSON )
    void getBalance(String account);

    @POST
    @Path("/extract")
    @Consumes(MediaType.APPLICATION_JSON )
    void getExtract(String account);

    @POST
    @Path("/transaction")
    @Consumes(MediaType.APPLICATION_JSON )
    void sendTransaction(String originAccount, String destinationAccount);

    @POST
    @Path("/total_value")
    @Consumes(MediaType.APPLICATION_JSON )
    void getTotalValue(String account, List<String> accounts);

    @POST
    @Path("/global_value")
    @Consumes(MediaType.APPLICATION_JSON )
    void getGlobalValue(String account);

    @POST
    @Path("/ledger")
    @Consumes(MediaType.APPLICATION_JSON )
    void getLedger(String account);

    @POST
    @Path("/mine")
    @Consumes(MediaType.APPLICATION_JSON )
    void mineBlock(String account, int difficulty);

}
