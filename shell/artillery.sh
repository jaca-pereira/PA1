#!/bin/bash

cd ../artillery
docker build -t artillery .

docker run --network net --ip "172.19.0.40" --name "start_0" -p 8080 -d artillery run --output report_start_0.json start_0.yml
#docker run --network net --ip "172.19.0.41" --name "start_1" -p 8080 -d artillery run --output report_start_1.json start_1.yml
#docker run --network net --ip "172.19.0.42" --name "start_2" -p 8080 -d artillery run --output report_start_2.json start_2.yml
#docker run --network net --ip "172.19.0.43" --name "start_3" -p 8080 -d artillery run --output report_start_3.json start_3.yml

sleep 7

docker run --network net --ip "172.19.0.40" --name "worload0" -p 8080 -d artillery run --output report_workload_0.json workload_0.yml
#docker run --network net --ip "172.19.0.41" --name "worload1" -p 8080 -d artillery run --output report_workload_1.json workload_1.yml
#docker run --network net --ip "172.19.0.42" --name "worload2" -p 8080 -d artillery run --output report_workload_2.json workload_2.yml
#docker run --network net --ip "172.19.0.43" --name "worload3" -p 8080 -d artillery run --output report_workload_3.json workload_3.yml

sleep 17

#docker run --network net --ip "172.19.0.40" --name "mine_0" -p 8080 -d artillery run --output report_mine_1.json mine_1.yml
#docker run --network net --ip "172.19.0.41" --name "mine_1" -p 8080 -d artillery run --output report_mine_2.json mine_2.yml
#docker run --network net --ip "172.19.0.42" --name "mine_2" -p 8080 -d artillery run --output report_mine_3.json mine_3.yml


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
