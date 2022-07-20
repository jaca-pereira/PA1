#!/bin/bash

if [ $# -lt 2 ] ; then
    echo "Usage: client.sh <address> <sgx>"
    exit 1
fi

S=$2
A=$1

cd ../client

docker build -t client .

docker run --name "client_$ID" -p 127.0.0.1:808${ID}:8080 -d client java -Djavax.net.ssl.trustStore=security/clientcacerts.jks -Djavax.net.ssl.trustStorePassword=password -cp client.jar client.Server $A $S

cd ../shell

