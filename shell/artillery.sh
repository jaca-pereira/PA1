#!/bin/bash

cd ../artillery
 
docker build -t artillery .

docker run --network net --ip "172.19.40.0" -p 8080 -it artillery run --output report_start.json start.yml
#docker run -t "workload_0" --network net --ip "172.19.40.0" -p 8080 -d -it artillery run --output report_workload_0.json workload_0.yml
#docker run -t "workload_1" --network net --ip "172.19.40.1" -p 8080 -d -it artillery run --output report_workload_1.json workload_1.yml
#docker run -t "workload_2" --network net --ip "172.19.40.2" -p 8080 -d -it artillery run --output report_workload_2.json workload_2.yml
#docker run -t "workload_3" --network net --ip "172.19.40.3" -p 8080 -d -it artillery run --output report_workload_3.json workload_3.yml



#docker run --network net --ip "172.19.40.0" -p 8080 -it artillery run --output report_bft_smart.json bftsmart.yml
#docker run --network net --ip "172.19.40.0" -p 8080 -it artillery run --output report_blockmess.json blockmess.yml
#docker run --network net --ip "172.19.40.0" -p 8080 -it artillery run --output report_blockchain.json blockchain.yml

cd ../shell
