package proxy;


import Security.InsecureHostNameVerifier;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;


import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import java.util.logging.Logger;

public class Server {
    private static Logger Log = Logger.getLogger(Server.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
    }

    public static final int PORT = 8080;

    public static void main(String [] args) throws UnknownHostException, NoSuchAlgorithmException, KeyManagementException {

        if (args.length < 2) {
            System.out.println("Usage: <proxyID> <blockmess>");
            System.exit(-1);
        }


        int id = Integer.parseInt(args[0]);
        int blm = Integer.parseInt(args[1]);
        boolean blockmess;
        if (blm == 0)
            blockmess = false;
        else blockmess = true;
        String ip = InetAddress.getLocalHost().getHostAddress();


        String serverURI = String.format("https://%s:%s/", ip, PORT);

        Service service = new Service(id, blockmess);

        ResourceConfig config = new ResourceConfig();
        config.register(service);
        HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostNameVerifier());
        JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config, SSLContext.getDefault());

        Log.info(String.format("%s Server ready @ %s\n",  InetAddress.getLocalHost().getCanonicalHostName(), serverURI));
    }

}
