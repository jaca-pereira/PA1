#!/bin/bash

if [ $# -lt 4 ] || [ $1 -gt 3  ] || [ $2 -gt 10  ] || [ $3 -gt 10  ]; then
    echo "Usage: deploy.sh <n_faults(max_faults=3)> <n_proxies(max_proxies==10)> <n_clients(max_clients=10)> <synch_asynch>"
    exit 1
fi

F=$1
P=$2
C=$3
A=$4

sh reset_containers.sh
sh config.sh $F
#sh security.sh
sh network.sh
sleep 1
sh replica.sh $F $D
sleep 5
sh proxy.sh $P $A
sleep 2
sh client.sh $C
#sleep 2
#sh artillery.sh