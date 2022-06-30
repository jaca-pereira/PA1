package client;



import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import test.Tests;

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
            System.out.println("Usage: <proxy_URI> <artillery>");
            System.exit(-1);
        }



        String ip = InetAddress.getLocalHost().getHostAddress();


        URI serverURI = URI.create(String.format("http://%s:%s/", ip, PORT));
        URI proxyURI = URI.create(args[0]);
        boolean artillery = Boolean.parseBoolean(args[1]);
        Client client = new Client(proxyURI, artillery);
        ResourceConfig config = new ResourceConfig();
        config.register(client);

        JdkHttpServerFactory.createHttpServer( serverURI, config);

        Log.info(String.format("%s Server ready @ %s\n",  InetAddress.getLocalHost().getCanonicalHostName(), serverURI));

    }

}
