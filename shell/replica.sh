#!/bin/bash

if [ $# -eq 0 ] || [ $1 -gt 3  ]; then
    echo "Usage: replica.sh <n_faults(max_faults==3)>"
    exit 1
fi

cd ../replica

mvn clean compile assembly:single

docker build -t replica .

F=$1
N=$((3*$F+1))

for i in `seq 0 $(( $N - 1 ))`; do

	docker run -d --network net --name "replica_$i" --ip "172.19.20.$i" replica java -cp replica.jar replicas.LedgerReplica $(( $i + 20 ))
	docker run -d --network net --name "redis_$i" --ip "172.19.30.$i" -p 6379 redis 

done

cd ../shell/
