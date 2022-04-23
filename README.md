# PA1
CSD PA1


Run client:
java -Djavax.net.ssl.trustStore=security/clientcacerts.jks -Djavax.net.ssl.trustStorePassword=password -cp target/PA1-1.0-SNAPSHOT-jar-with-dependencies.jar client.ClientClass 127.0.1.1 8080

Run server:
java -Djavax.net.ssl.keyStore=security/serverkeystore.jks -Djavax.net.ssl.keyStorePassword=password -cp target/PA1-1.0-SNAPSHOT-jar-with-dependencies.jar proxy.Server 4
