#!/bin/bash

if [ $# -eq 0 ] ; then
    echo "Usage: bft_smart.sh <id>"
    exit 1
fi

ID=$1

cd ../replica

docker build -t replica .

cd ../proxy

docker build -t proxy .

cd ../shell

docker rm $(docker stop redis_g2)
docker run -d --network net --name "redis_g2" --ip "172.19.20.0" redis 
sleep 2
docker rm $(docker stop replica_g2)
docker run -d --network net --name "replica_g2" --ip "172.19.20.1" -p 20001:20001 -p 20010:20010 replica java -cp replica.jar replicas.LedgerReplica $ID
sleep 3
docker rm $(docker stop proxy_g2)
docker run -d --network net --name "proxy_g2" --ip "172.19.20.2" -p 20000:20000 proxy java -Djavax.net.ssl.keyStore=security/serverkeystore.jks -Djavax.net.ssl.keyStorePassword=password -cp server.jar proxy.Server $(( $ID + 10 )) 0 0 0





