# PA1
CSD PA1

Sequential clients with no persistency:

	Run shell/run_app_without_client.sh for starting docker containers.
	Run client:
		mvn clean compile assembly:single
		java -Djavax.net.ssl.trustStore=security/clientcacerts.jks -Djavax.net.ssl.trustStorePassword=password -cp target/PA1-CLIENT-1.0-SNAPSHOT-jar-with-dependencies.jar client.OperationsTestClient 172.18.0.
	Results file in client folder.
	





