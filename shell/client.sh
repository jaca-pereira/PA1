#!/bin/bash

if [ $# -eq 0 ] || [ $1 -gt 8  ]; then
    echo "Usage: client.sh <n_clients(max_clients=8)>"
    exit 1
fi

C=$1


cd ../client

mvn clean compile assembly:single

docker build -t client .

for i in `seq 0 $(( $C - 1  ))`; do 

    docker run --network net --ip "172.19.0.$(($i + 2))" --name "client_$i" -p 8080 -d client java -Djavax.net.ssl.trustStore=security/clientcacerts.jks -Djavax.net.ssl.trustStorePassword=password -cp client.jar client.Server "https://172.19.10.${i}:8080"

done
cd ../shell

