FROM openjdk:11
WORKDIR .
COPY target/PA1-CLIENT-1.0-SNAPSHOT-jar-with-dependencies.jar client.jar
CMD ["java", "-Djavax.net.ssl.trustStore=security/clientcacerts.jks", "-Djavax.net.ssl.trustStorePassword=password", "-cp", "client.jar", "client.ClientClass", "172.18.0.2", "8080"]
