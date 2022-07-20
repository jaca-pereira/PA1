#!/bin/bash

if [ $# -eq 0 ] || [ $1 -gt 8  ]; then
    echo "Usage: client.sh <n_clients(max_clients=8)> <sgx>"
    exit 1
fi

C=$1
S=$2

cd ../client

docker build -t client .

for i in `seq 0 $(( $C - 1  ))`; do 

    docker run --name "client_$i" -p 127.0.0.1:808${i}:8080 -d client java -Djavax.net.ssl.trustStore=security/clientcacerts.jks -Djavax.net.ssl.trustStorePassword=password -cp client.jar client.Server $i $S

done
cd ../shell

