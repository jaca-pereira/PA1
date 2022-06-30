#!/bin/bash

if [ $# -lt 4 ] || [ $1 -gt 10  ] || [ $2 -gt 10  ] ; then
    echo "Usage: deploy.sh <n_proxies(max_proxies==10)> <n_clients(max_clients=10)> <blockmess> <artillery> <n_faults(max_faults=3)> "
    exit 1
fi

F=$5
P=$1
C=$2
B=$3
A=$4

sh reset_containers.sh
#sh security.sh chaves ja criadas, não é necessário correr
sh network.sh
sleep 1

if [ $B -eq 0 ] ; then
    sh config.sh $F
    sh replica.sh $F
    sleep 5
    sh proxy.sh $P
    sleep 2
fi

if [ $B -eq 1 ] ; then
    sh blockmess.sh $P
    sleep 5
fi

sh client.sh $C $A
if [ $A -eq 1 ] ; then
    sleep 2
    sh artillery.sh
fi