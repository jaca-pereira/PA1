#!/bin/bash

if [ $# -lt 4 ] ; then
    echo "Usage: blockmess.sh <id> <n_nodes> <address> <sgx>"
    exit 1
fi

ID=$1
N=$2
A=$3
S=$4

cd ../proxy

docker build -t proxy .

cd ../shell

docker rm $(docker stop redis_g2)
docker run -d --network net --name "redis_g2" --ip "172.19.20.0" redis 
sleep 2
docker rm $(docker stop proxy_g2)
docker run -d --network net --name "proxy_g2" --ip "172.19.20.2" -p 20000:20000 proxy java -Djavax.net.ssl.keyStore=security/serverkeystore.jks -Djavax.net.ssl.keyStorePassword=password -cp server.jar proxy.Server $ID 1 $N $A

if [ $S -eq 1 ] ; then
    docker run -d --network net --name "sconify_sgx_g2" --ip "172.19.20.3" -p 20002:20002 sconify_sgx_g2 java -cp server.jar proxy.Server $A
fi






