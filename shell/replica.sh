#!/bin/bash

cd ../replica

mvn clean compile assembly:single

docker build -t replica .

for i in `seq 4`; do

	docker run -d --network net --name "replica_$(( $i - 1 ))" --ip "172.18.0.$(( $i + 5 ))" replica java -cp replica.jar replicas.LedgerReplica $(( $i - 1 ))

done

