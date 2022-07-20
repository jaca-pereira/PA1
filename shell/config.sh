#!/bin/sh
if [ $# -eq 0 ] || [ $1 -gt 3  ]; then
    echo "Usage: replica.sh <n_faults(max_faults==3)> <proxy>"
    exit 1
fi


F=$1
N=$((3*$F+1))
P=$2

cd ../configs/
cp hosts.template hosts.config
cp system.template system.config

echo "system.initial.view = $(seq -s ',' 0 $(( $N - 1  )) )" >> system.config
echo "system.servers.num = $N" >> system.config
echo "system.servers.f = $F" >> system.config
if [ $P -eq 0 ]; then
    for i in `seq 0 $(( $N - 1 ))`; do
        echo "$i 172.19.20.$i 20000 20010" >> hosts.config
    done
    echo "7001 127.0.0.1 20099" >> hosts.config
    cp hosts.config ../replica/config
    cp system.config ../replica/config  
fi
if [ $P -eq 1 ]; then
    for i in `seq 0 $(( $N - 1 ))`; do
        echo "$i 141.95.173.56 2000$i 20010" >> hosts.config
    done
    echo "7001 127.0.0.1 20099" >> hosts.config
    cp hosts.config ../proxy/config
    cp system.config ../proxy/config
    cp config.properties ../proxy/config
    cp log4j2.xml ../proxy/log4j2.xml
fi
cd ../shell


