#!/bin/bash

if [ $# -lt 3 ] ; then
    echo "Usage: blockmess.sh <id> <n_nodes> <address>"
    exit 1
fi

ID=$1
N=$2
A=$3

cd ../proxy

docker build -t proxy .

cd ../shell

docker rm $(docker stop redis_g2)
docker run -d --network net --name "redis_g2" --ip "172.19.20.0" redis 
sleep 2
docker rm $(docker stop proxy_g2)
docker run -d --network net --name "proxy_g2" --ip "172.19.20.2" -p 20000:20000 proxy java -Djavax.net.ssl.keyStore=security/serverkeystore.jks -Djavax.net.ssl.keyStorePassword=password -cp server.jar proxy.Server $ID 1 $N $A







