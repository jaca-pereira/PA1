#!/bin/bash

if [ $# -eq 0 ]  [ $1 -gt 10  ]; then
    echo "Usage: client.sh <n_clients(max_clients=10)> <n_users>"
    exit 1
fi

C=$1
U=$2


cd ../client_prepare

mvn clean compile assembly:single
java -cp PA1-CLIENT-PREPARE-1.0-SNAPSHOT-jar-with-dependencies.jar Main

cd ../client

mvn clean compile assembly:single

docker build -t client .

for i in `seq 0 $(( $C - 1 ))`; do

	docker run --network net --ip "172.18.0.$i" --name "client_$i" -p 8080 -d client java -cp client.jar client.Server $U

done
	
cd ../shell

