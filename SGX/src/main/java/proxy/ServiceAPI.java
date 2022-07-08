package proxy;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
public interface ServiceAPI {

    @POST
    @Path("/transaction")
    @Consumes(MediaType.APPLICATION_JSON)
    byte[] sendTransaction(byte[] data);

}
