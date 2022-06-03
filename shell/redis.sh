#!/bin/bash

for i in `seq 4`; do
	docker run --network net --name "redis" --ip "172.18.0.21" -d redis 

