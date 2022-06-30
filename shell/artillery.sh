#!/bin/bash

cd ../artillery
docker build -t artillery .

docker run --network net --ip "172.19.0.40" --name "start_0" -p 8080 artillery run --output report_start_0.json start_0.yml
docker run --network net --ip "172.19.0.41" --name "start_1" -p 8080 -d artillery run --output report_start_1.json start_1.yml
docker run --network net --ip "172.19.0.42" --name "start_2" -p 8080 -d artillery run --output report_start_2.json start_2.yml
docker run --network net --ip "172.19.0.43" --name "start_3" -p 8080 -d artillery run --output report_start_3.json start_3.yml

sleep 7

docker run --network net --ip "172.19.0.40" --name "worload_0" -p 8080 artillery run --output report_workload_0.json workload_0.yml
docker run --network net --ip "172.19.0.41" --name "worload_1" -p 8080 -d artillery run --output report_workload_1.json workload_1.yml
docker run --network net --ip "172.19.0.42" --name "worload_2" -p 8080 -d artillery run --output report_workload_2.json workload_2.yml
docker run --network net --ip "172.19.0.43" --name "worload_3" -p 8080 -d artillery run --output report_workload_3.json workload_3.yml

sleep 17

docker run --network net --ip "172.19.0.40" --name "mine_0" -p 8080 -d artillery run --output report_mine_0.json mine_0.yml

#artillery report report_start_0.json
#artillery report report_start_1.json
#artillery report report_start_2.json
#artillery report report_start_3.json

#artillery report report_workload_0.json
#artillery report report_workload_1.json
#artillery report report_workload_2.json
#artillery report report_workload_3.json

#artillery report report_mine.json


cd ../shell
