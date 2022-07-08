package data;


import Security.InsecureHostNameVerifier;
import Security.Security;
import org.glassfish.jersey.client.ClientConfig;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;


public class Client {

    private final URI proxyURI;
    private final javax.ws.rs.client.Client client;

    public Client(URI proxyURI) {
        this.proxyURI = proxyURI;
        this.client = this.startClient();
    }

    private javax.ws.rs.client.Client startClient() {

        try {
            SSLContext context = Security.getContext();
            HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostNameVerifier());
            HttpsURLConnection.setDefaultSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
            ClientConfig config = new ClientConfig();
            return ClientBuilder.newBuilder().sslContext(context)
                    .withConfig(config).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] sendTransaction(byte[] request) {
        try {
            WebTarget target = this.client.target(proxyURI).path("transaction");

            Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
            if(  r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
                return r.readEntity(byte[].class);

            } else
                throw new WebApplicationException(r.getStatus());
        } catch ( ProcessingException e) {
            throw new InternalServerErrorException();
        }
    }
}

