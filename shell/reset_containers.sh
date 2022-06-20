#!/bin/sh
docker rm $(docker stop $(docker ps -a -q ))

docker image rmi proxy
docker image rmi redis
docker image rmi replica
docker image rmi client
docker image rmi artilleryio/artillery

docker network remove net
