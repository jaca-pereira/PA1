#!/bin/bash

if [ $# -eq 0 ] || [ $1 -gt 10  ]; then
    echo "Usage: proxy.sh <n_proxies(max_proxies==10)>"
    exit 1
fi

N=$1

cd ../proxy

docker build -t proxy .

for i in `seq 0 $(( $N - 1 ))`; do

	docker run --name "proxy_$i" -p 2000$i:20000 -d proxy java -Djavax.net.ssl.keyStore=security/serverkeystore.jks -Djavax.net.ssl.keyStorePassword=password -cp server.jar proxy.Server $(( $i + 10 )) 0 $N
	
done

cd ../shell


