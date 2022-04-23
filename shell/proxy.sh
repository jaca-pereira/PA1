#!/bin/bash

cd ../proxy

mvn clean compile assembly:single

docker build -t proxy .

docker run --network net --ip "172.18.0.2" -p 8080 -t proxy 
