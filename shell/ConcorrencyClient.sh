#!/bin/bash

run_app_without_client.sh 

cd ../client

mvn clean compile assembly:single

java -Djavax.net.ssl.trustStore=security/clientcacerts.jks -Djavax.net.ssl.trustStorePassword=password -cp target/PA1-CLIENT-1.0-SNAPSHOT-jar-with-dependencies.jar client.ConcurrencyClient 172.18.0.
