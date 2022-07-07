#!/bin/bash

if [ $# -lt 3 ] ; then
    echo "Usage: deploy.sh <<n_clients n_proxies <blockmess> <n_faults> "
    exit 1
fi

C=$1
P=$2
B=$3
F=$4
N=$((3*$F+1))

sh reset_containers.sh
#sh security.sh chaves ja criadas, não é necessário correr
cd ../
cp configs/compose.template compose.yaml

for i in `seq 0 $(( $C - 1 ))`; do
    echo "  client_${i}:
    image: client
    ports: 
      - \"127.0.0.1:808${i}:8080\"
    build:
      context: /client
    depends_on:
      - proxy_${i}
    command: java -Djavax.net.ssl.trustStore=security/clientcacerts.jks -Djavax.net.ssl.trustStorePassword=password -cp client.jar client.Server $i
    " >> compose.yaml
done

for i in `seq 0 $(( $P - 1 ))`; do
    echo "  proxy_${i}:
    image: proxy
    build:
      context: /proxy
    depends_on: 
      - redis_${i}
    command: java -Djavax.net.ssl.keyStore=security/serverkeystore.jks -Djavax.net.ssl.keyStorePassword=password -cp server.jar proxy.Server $i $B $P
    " >> compose.yaml
done

if [ $B -eq 0 ] ; then
    cd configs/
    cp hosts.template hosts.config
    cp system.template system.config

    echo "system.initial.view = $(seq -s ',' 0 $(( $N - 1  )) )" >> system.config
    echo "system.servers.num = $N" >> system.config
    echo "system.servers.f = $F" >> system.config
    for i in `seq 0 $(( $N - 1 ))`; do
        echo "$i replica_${i} 11000 11001" >> hosts.config
    done
    echo "7001 127.0.0.1 11100" >> hosts.config

    cp hosts.config ../replica/config
    cp system.config ../replica/config
    cp hosts.config ../proxy/config
    cp system.config ../proxy/config
    cp hosts.config ../client/config
    cp system.config ../client/config
    
    cd ..
    for i in `seq 0 $(( $N - 1 ))`; do
      echo "  replica_${i}:
    image: replica
    build:
      context: /replica
    depends_on: 
      - redis_${i}
    command: java -cp replica.jar replicas.LedgerReplica $i
      " >> compose.yaml
    done

    for i in `seq 0 $(( $N - 1 ))`; do
      echo "  redis_${i}:
    image: redis
      " >> compose.yaml
    done
fi

if [ $B -eq 1 ] ; then
    cd configs/
    cp config.properties ../proxy/config
    cp log4j2.xml ../proxy/log4j2.xml
    cd ..
    for i in `seq 0 $(( $N - 1 ))`; do
      echo "  redis_${i}:
    image: redis
      " >> compose.yaml
    done
fi

docker compose build --no-cache
docker compose up -d

cd shell
