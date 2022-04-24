#!/bin/bash

cd ../replica

mvn clean compile assembly:single

for i in `seq 4`; do

	docker build -t replica .

	docker run -d --name "replica_($i-1)" replica java -cp replica.jar replicas.LedgerReplica ($i-1)

done

