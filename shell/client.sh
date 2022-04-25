#!/bin/bash

cd ../client

mvn clean compile assembly:single

docker build -t client .

docker run --network net --name "client" --ip "172.18.0.20" -p 8080 -ti client java -Djavax.net.ssl.trustStore=security/clientcacerts.jks -Djavax.net.ssl.trustStorePassword=password -cp client.jar client.ClientClass 172.18.0.11 4
