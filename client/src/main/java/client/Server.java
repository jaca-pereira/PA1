package client;



import bftsmart.communication.server.Test;
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

        if (args.length < 3) {
            System.out.println("Usage: <address> <sgx> <mine>");
            System.exit(-1);
        }



        String ip = InetAddress.getLocalHost().getHostAddress();

        String address = args[0];
        URI serverURI = URI.create(String.format("http://%s:%s/", ip, PORT));

        String proxyURI = "https://" + address + ":20000/";
        System.out.println(proxyURI);
        Integer sgx = Integer.parseInt(args[1]);
        Client client;
        if (sgx == 0 )
         client= new Client(proxyURI, false);
        else  {
            String sgxURI = "https://" + address + ":20002/";
            client = new Client(proxyURI, true, sgxURI);
        }
        ResourceConfig config = new ResourceConfig();
        config.register(client);

        JdkHttpServerFactory.createHttpServer( serverURI, config);
        Log.info(String.format("%s Server ready @ %s\n",  InetAddress.getLocalHost().getCanonicalHostName(), serverURI));

        if (Integer.parseInt(args[2]) == 1) {
            TestMine testMine = new TestMine(client);
            testMine.test();
        }


    }

}
