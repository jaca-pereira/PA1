#!/bin/bash

if [ $# -lt 4 ] || [ $1 -gt 10  ] || [ $2 -gt 10  ] ; then
    echo "Usage: deploy.sh <n_clients(max_clients=10)> <n_proxies(max_proxies==10)> <blockmess> <sgx> <n_faults(max_faults=3)> "
    exit 1
fi

F=$5
P=$2
C=$1
B=$3
S=$4

sh reset_containers.sh
#sh security.sh chaves ja criadas, não é necessário correr
sh network.sh
sleep 1
sh config.sh $F

if [ $B -eq 0 ] ; then
    sh replica.sh $F
    sleep 2
    sh proxy.sh $P
fi

if [ $B -eq 1 ] ; then
    sh blockmess.sh $P
    sleep 2
fi


sh client.sh $C $S
