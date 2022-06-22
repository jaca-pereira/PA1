#!/bin/sh
if [ $# -eq 0 ] || [ $1 -gt 3  ]; then
    echo "Usage: replica.sh <n_faults(max_faults==3)>"
    exit 1
fi


F=$1
N=$((3*$F+1))

cd ../configs/
cp hosts.template hosts.config
cp system.template system.config

echo "system.initial.view = $(seq -s ',' 20 $(( $N - 1 + 20 )) )" >> system.config
echo "system.servers.num = $N" >> system.config
echo "system.servers.f = $F" >> system.config
for i in `seq 20 $(( $N - 1 + 20 ))`; do
    echo "$i 172.19.0.$(($i + 20)) 11000 11001" >> hosts.config
done
echo "7001 127.0.0.1 11100" >> hosts.config


cp hosts.config ../replica/config
cp system.config ../replica/config
cp hosts.config ../proxy/config
cp system.config ../proxy/config
cp hosts.config ../client/config
cp system.config ../client/config


cd ../security

keytool -genkey -alias server -keyalg RSA -keypass password -keystore serverkeystore.jks -storepass password -dname "CN=FCT, OU=DI, L=ALMADA"

keytool -export -alias server -file serverkey.cer -keystore serverkeystore.jks -storepass password

keytool -import -v -trustcacerts -alias clientTrust -keypass password -file serverkey.cer -keystore clientcacerts.jks -storepass password

cd ../shell


