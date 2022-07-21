#!/bin/bash

if [ $# -lt 6 ] ; then
    echo "Usage: deploy.sh <client> <address> <id> <nodes_blockmess> <sgx> <test_mine>"
    exit 1
fi

C=$1
ID=$3
B=$4
S=$5
A=$2
M=$6

if [ $C -eq 1 ] ; then
    sh client.sh $ID $A $S $M
fi
if [ $C -eq 0 ] ; then
    docker network remove net
    docker network create --driver=bridge --subnet=172.19.0.0/16 net
    if [ $B -eq 0 ] ; then
        sh bft_smart.sh $ID $S $A
    fi
    if [ $B -gt 0 ] ; then
        sh blockmess.sh $ID $B $A $S
    fi    
fi

