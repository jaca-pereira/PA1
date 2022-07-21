#!/bin/bash

if [ $# -lt 4 ] ; then
    echo "Usage: client.sh <id> <address> <sgx> <testMine>"
    exit 1
fi
ID=$1
S=$3
A=$2
M=$4

cd ../client

docker build -t client .

docker run --name "client_$ID" -p 127.0.0.1:808${ID}:8080 -d client java -Djavax.net.ssl.trustStore=security/clientcacerts.jks -Djavax.net.ssl.trustStorePassword=password -cp client.jar client.Server $A $S $M

cd ../shell

