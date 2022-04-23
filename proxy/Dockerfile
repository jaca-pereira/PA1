FROM openjdk:11
WORKDIR .
COPY config config
COPY security security
COPY target/PA1-SERVER-1.0-SNAPSHOT-jar-with-dependencies.jar server.jar
CMD ["java", "-Djavax.net.ssl.keyStore=security/serverkeystore.jks", "-Djavax.net.ssl.keyStorePassword=password", "-cp", "server.jar", "proxy.Server", "0"]
