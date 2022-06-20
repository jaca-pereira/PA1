#!/bin/sh
if [ $# -eq 0 ] || [ $1 -gt 3  ]; then
    echo "Usage: replica.sh <n_faults(max_faults==3)>"
    exit 1
fi


F=$1
N=$((3*$F+1))

cd configs/
cp hosts.template hosts.config
cp system.template system.config

echo "system.initial.view = $(seq -s ',' 0 $(( $N - 1 )) )" >> system.config
echo "system.servers.num = $(( $N ))" >> system.config
echo "system.servers.f = $F" >> system.config
for i in `seq 0 $(( $N - 1 ))`; do
    echo "$i 172.18.0.$($i + 20) 11000 11001" >> hosts.config
done
echo "7001 127.0.0.1 11100"


cp hosts.config ../replica/config
cp system.config ../replica/config
cp hosts.config ../proxy/config
cp system.config ../proxy/config
cp hosts.config ../client/config
cp system.config ../client/config

cd ..


