#!/bin/sh
docker rm $(docker stop $(docker ps -a -q --filter ancestor=replica))
docker rm $(docker stop $(docker ps -a -q --filter ancestor=proxy))
docker rm $(docker stop $(docker ps -a -q --filter ancestor=client))

docker image rmi proxy
docker image rmi replica
docker image rmi client

docker network remove net
