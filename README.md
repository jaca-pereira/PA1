# PA1
CSD PA1

Sequential clients with no persistency:
	
	Make sure pom.xml in client/ has NoConcurrencyClient as main class.
	Run shell/run_app_without_client.sh for starting docker containers.
	Run in /client:
		mvn clean compile assembly:single
		java -Djavax.net.ssl.trustStore=security/clientcacerts.jks -Djavax.net.ssl.trustStorePassword=password -cp target/PA1-CLIENT-1.0-SNAPSHOT-jar-with-dependencies.jar client.NoConcurrencyClient 172.18.0.
	Results file in client folder.
	
Concurrent clients with no persistency:
	
	Make sure pom.xml in client/ has ConcurrencyClient as main class.
	Run shell/run_app_without_client.sh for starting docker containers.
	Run in /client:
		mvn clean compile assembly:single
		java -Djavax.net.ssl.trustStore=security/clientcacerts.jks -Djavax.net.ssl.trustStorePassword=password -cp target/PA1-CLIENT-1.0-SNAPSHOT-jar-with-dependencies.jar client.ConcurrencyClient 172.18.0.
	Results file in client folder.
	





