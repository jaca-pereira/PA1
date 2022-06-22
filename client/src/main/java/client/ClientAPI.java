package client;


import javax.ws.rs.*;
import java.util.List;

@Path("/")
public interface ClientAPI {

    @POST
    @Path("/balance")
    void getBalance(String account);

    @POST
    @Path("/extract")
    void getExtract(String account);

    @POST
    @Path("/transaction")
    void sendTransaction(String originAccount, String destinationAccount);

    @POST
    @Path("/total_value")
    void getTotalValue(String account, List<String> accounts);

    @POST
    @Path("/global_value")
    void getGlobalValue(String account);

    @POST
    @Path("/ledger")
    void getLedger(String account);

    @POST
    @Path("/mine")
    void mineBlock(String account, int difficulty);

}
