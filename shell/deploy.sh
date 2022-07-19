#!/bin/bash

if [ $# -lt 5 ] || [ $1 -gt 10  ] || [ $2 -gt 10  ] || [ $3 -gt 10  ]; then
    echo "Usage: deploy.sh <n_clients(max_clients=10)> <n_proxies(max_proxies==10)> <n_faults(max_faults=3)> <blockmess> <sgx>"
    exit 1
fi

F=$3
P=$2
C=$1
B=$3
S=$4

sh reset_containers.sh
#sh security.sh chaves ja criadas, não é necessário correr

sleep 1

if [ $C -gt 0 ] ; then
    sh client.sh $C $S
    sh artillery.sh
fi
if [ $P -gt 0 ] ; then
    sh config.sh $F
    if [ $B -eq 0 ] ; then
        sh proxy.sh $P 1
    fi
    if [ $B -eq 1 ] ; then
        sh network.sh
        sh blockmess.sh $P
    fi    
fi
if [ $F -gt 0 ] ; then
    sh network.sh
    sh config.sh $F
    sh replica.sh $F 0
fi

