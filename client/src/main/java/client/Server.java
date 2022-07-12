package client;



import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.security.NoSuchAlgorithmException;

import java.util.logging.Logger;

public class Server {
    private static Logger Log = Logger.getLogger(Server.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
    }

    public static final int PORT = 8080;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        if (args.length < 2) {
            System.out.println("Usage: <proxy> <sgx>");
            System.exit(-1);
        }



        String ip = InetAddress.getLocalHost().getHostAddress();


        URI serverURI = URI.create(String.format("http://%s:%s/", ip, PORT));

        String proxyURI = "https://172.19.10." + args[0] + ":8080/";
        System.out.println(proxyURI);
        int sgx = Integer.parseInt(args[1]);
        Client client;
        if (sgx==0)
         client= new Client(proxyURI, false);
        else  {
            String sgxURI = "https://172.19.40." + args[0] + ":8080/";
            client = new Client(proxyURI, true, sgxURI);
        }
        ResourceConfig config = new ResourceConfig();
        config.register(client);

        JdkHttpServerFactory.createHttpServer( serverURI, config);

        Log.info(String.format("%s Server ready @ %s\n",  InetAddress.getLocalHost().getCanonicalHostName(), serverURI));

    }

}
