#!/bin/bash

if [ $# -eq 0 ] || [ $1 -gt 10  ]; then
    echo "Usage: proxy.sh <n_proxies(max_proxies==10)>"
    exit 1
fi

N=$1

cd ../configs

cp proxies.template proxies.config

for i in `seq 0 $(( $N - 1 ))`; do
    echo "https:/172.18.0.$(($i + 10)):8080/" >> proxies.config
done


cd ../proxy

mvn clean compile assembly:single

docker build -t proxy .

for i in `seq 0 $(( $N - 1 ))`; do
		
	docker run --network net --ip "172.18.0.$(( $i + 10 ))" --name "proxy_$i" -p 8080 -d proxy java -Djavax.net.ssl.keyStore=security/serverkeystore.jks -Djavax.net.ssl.keyStorePassword=password -cp server.jar proxy.Server $i
	
done

cd ../shell


