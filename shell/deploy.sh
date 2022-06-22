#!/bin/bash

if [ $# -eq 0 ] || [ $1 -gt 3  ] || [ $2 -gt 10  ] || [ $3 -gt 10  ]; then
    echo "Usage: deploy.sh <n_faults(max_faults=3)> <n_proxies(max_proxies==10)> <n_clients(max_clients=10)> <n_users>"
    exit 1
fi

F=$1
P=$2
C=$3

sh reset_containers.sh
sh config.sh $F
sh network.sh
sh replica.sh $F
sh proxy.sh $P
sh client.sh $C
sh artillery.sh
