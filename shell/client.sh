#!/bin/bash

cd ../client

mvn clean compile assembly:single

docker build -t client .

docker run --network net --ip "172.18.0.3" -p 8080 -t client 
