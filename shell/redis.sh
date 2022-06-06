#!/bin/bash

for i in `seq 4`; do
	docker run --network net --name "redis$(($i -1))" --ip "172.18.0.2$(($i -1))" -d redis 
done

