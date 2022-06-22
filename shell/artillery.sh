#!/bin/bash

cd ../artillery
 
docker build -t artillery .

docker run --network net --ip "172.19.40.0" -p 8080 -it artillery run --output test-run-report.json create_accounts.yml

cd ../shell
