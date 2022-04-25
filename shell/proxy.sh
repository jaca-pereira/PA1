#!/bin/bash

cd ../proxy

mvn clean compile assembly:single

docker build -t proxy .

for i in `seq 4`; do

	docker run --network net --ip "172.18.0.$(( $i + 9 ))" --name "proxy_$(( $i + 3 ))" -p 8080 -d proxy java -Djavax.net.ssl.keyStore=security/serverkeystore.jks -Djavax.net.ssl.keyStorePassword=password -cp server.jar proxy.Server $(( $i + 3 ))
	
done


