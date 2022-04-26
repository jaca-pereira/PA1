## CSD PA1

**Deployment guide:**

Sequential clients:
	
* Make sure pom.xml in client/ dir has NoConcurrencyClient as main class.
* Run 

		shell/run_app_without_client.sh 

for starting docker containers.

* Run in /client dir:
				
		mvn clean compile assembly:single
		java -Djavax.net.ssl.trustStore=security/clientcacerts.jks -Djavax.net.ssl.trustStorePassword=password -cp target/PA1-CLIENT-1.0-SNAPSHOT-jar-with-dependencies.jar client.NoConcurrencyClient 172.18.0.
				
* Results file:
 
		/client/results.txt
	
Concurrent clients:
	
* Make sure pom.xml in client/ dir has ConcurrencyClient as main class.
* Run 
		
		shell/run_app_without_client.sh 
		
for starting docker containers.

* Run in /client:

		mvn clean compile assembly:single
		java -Djavax.net.ssl.trustStore=security/clientcacerts.jks -Djavax.net.ssl.trustStorePassword=password -cp target/PA1-CLIENT-1.0-SNAPSHOT-jar-with-dependencies.jar client.ConcurrencyClient 172.18.0.
		
* Results files:

		/client/results_concurrent1.txt
		/client/results_concurrent2.txt
		/client/results_concurrent3.txt
		/client/results_concurrent4.txt

Concurrent clients and concurrent proxies:
	
* Make sure pom.xml in client/ dir has ConcurrencyClientConcurrentProxies as main class.
* Run 

		shell/run_app_without_client.sh 
		
for starting docker containers.

* Run in /client:

		mvn clean compile assembly:single
		java -Djavax.net.ssl.trustStore=security/clientcacerts.jks -Djavax.net.ssl.trustStorePassword=password -cp target/PA1-CLIENT-1.0-SNAPSHOT-jar-with-dependencies.jar client.ConcurrencyClientConcurrentProxies 172.18.0.
		
* Results file:

		/client/results_concurrent_proxies1.txt
		/client/results_concurrent_proxies2.txt
		/client/results_concurrent_proxies3.txt
		/client/results_concurrent_proxies4.txt

Test Single Replica fail:
	
* Do sequential clients. 
* Run 
		stop_replica.sh
		
* Change pom.xml in client/ dir and make sure main class is ClientAfterReplicaFail
* Run in /client dir:

		mvn clean compile assembly:single
		java -Djavax.net.ssl.trustStore=security/clientcacerts.jks -Djavax.net.ssl.trustStorePassword=password -cp target/PA1-CLIENT-1.0-SNAPSHOT-jar-with-dependencies.jar client.ClientAfterReplicaFail 172.18.0.
		
* Results file: 

		/client/results_replica_fail.txt


Clients are not included in docker because results files wouldn't persist.

Keystore and truststore for client already included. To generate new ones, run instructions in Keystore and Truststore generation.

Note: results files may be off in Transactions because serialization is not well performed when printing but the success of the operations demonstrates that they are being well saved.

	






