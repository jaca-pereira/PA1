#!/bin/bash

if [ $# -eq 0 ] || [ $1 -gt 8  ]; then
    echo "Usage: client.sh <n_clients(max_clients=8)> <n_users> <nr_proxies_per_client>"
    exit 1
fi

C=$1
U=$2


cd ../client

cp ../security/clientcacerts.jks security/clientcacerts.jks


cd security/

for i in `seq 0 $U`; do
	keytool -genkeypair -alias "user_$i" -keyalg EC -keysize 256 -keystore "${i}keystore.jks" -storepass "password" -dname "CN=FCT, OU=DI, L=ALMADA"
done

cd ..

mvn clean compile assembly:single

docker build -t client .

for i in `seq 2 $(( $C - 1 +2 ))`; do #172.19.0.1 está ocupado, saltamos esse à frente

	docker run --network net --ip "172.19.0.$i" --name "client_$i" -p 8080 -d client java -cp client.jar client.Server $i "https://172.19.10.${i}:8080"

done
	
cd ../shell

