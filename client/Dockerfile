FROM openjdk:11
WORKDIR .

COPY config config
COPY security security
COPY ../configs/proxies.config proxies.config
COPY ../configs/keys.config keys.config 

COPY target/PA1-CLIENT-1.0-SNAPSHOT-jar-with-dependencies.jar client.jar
