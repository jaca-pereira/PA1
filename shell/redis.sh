#!/bin/bash

docker run --network net --name "redis" --ip "172.18.0.21" -d redis 
#redis-server --save 30 1 --loglevel warning
