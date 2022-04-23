# PA1
CSD PA1


Run client:
java -Djavax.net.ssl.trustStore=security/clientcacerts.jks -Djavax.net.ssl.trustStorePassword=password -cp target/PA1-CLIENT-1.0-SNAPSHOT-jar-with-dependencies.jar client.ClientClass 127.0.1.1 8080

Run server:
java -Djavax.net.ssl.keyStore=security/serverkeystore.jks -Djavax.net.ssl.keyStorePassword=password -cp target/PA1-SERVER-1.0-SNAPSHOT-jar-with-dependencies.jar proxy.Server 0

Run Replica:
java -cp target/PA1-REPLICA-1.0-SNAPSHOT-jar-with-dependencies.jar replicas.LedgerReplica 1
java -cp target/PA1-REPLICA-1.0-SNAPSHOT-jar-with-dependencies.jar replicas.LedgerReplica 2
java -cp target/PA1-REPLICA-1.0-SNAPSHOT-jar-with-dependencies.jar replicas.LedgerReplica 3
java -cp target/PA1-REPLICA-1.0-SNAPSHOT-jar-with-dependencies.jar replicas.LedgerReplica 4

