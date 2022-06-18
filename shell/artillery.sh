#!/bin/bash
docker build -t artillery
docker run --network net --ip "172.18.0.40" --name "artillery" -p 8080 artillery 
