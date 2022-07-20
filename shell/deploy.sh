#!/bin/bash

if [ $# -lt 5 ] ; then
    echo "Usage: deploy.sh <client> <address> <id> <nodes_blockmess> <sgx> "
    exit 1
fi

C=$1
ID=$3
B=$4
S=$5
A=$2

if [ $C -eq 1 ] ; then
    docker rm $(docker stop $(docker ps -a -q ))
    sh client.sh $ID $A $S
fi
if [ $C -eq 0 ] ; then
    docker network remove net
    docker network create --driver=bridge --subnet=172.19.0.0/16 net
    if [ $B -eq 0 ] ; then
        sh bft_smart.sh $ID
    fi
    if [ $B -gt 0 ] ; then
        sh blockmess.sh $ID $B $A
    fi    
fi

