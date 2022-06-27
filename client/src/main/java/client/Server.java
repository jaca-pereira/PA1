package client;



import test.Tests;

import java.io.IOException;
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

        /*if (args.length < 1) {
            System.out.println("Usage: <proxy_URI>");
            System.exit(-1);
        }
        */


        //String ip = InetAddress.getLocalHost().getHostAddress();


        //URI serverURI = URI.create(String.format("http://%s:%s/", ip, PORT));
        //URI proxyURI = URI.create(args[0]);
        //Client client = new Client(proxyURI);
        Tests tests = new Tests();
        tests.testGeneral();

        while(true);
        //ResourceConfig config = new ResourceConfig();
        //config.register(tests);

        //JdkHttpServerFactory.createHttpServer( serverURI, config);

        //Log.info(String.format("%s Server ready @ %s\n",  InetAddress.getLocalHost().getCanonicalHostName(), serverURI))

    }

}
