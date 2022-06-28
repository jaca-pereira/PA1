#!/bin/bash

cd ../artillery
#docker build -t start .
#docker run --network net --ip "172.19.0.40" --name "start" -p 8080 start run --output report_start_0.json start_0.yml

#docker run --network net --ip "172.19.0.40" --name "start1" -p 8080 start run --output report_start_1.json start_1.yml
#docker run --network net --ip "172.19.0.40" --name "start2" -p 8080 start run --output report_start_2.json start_2.yml
#docker run --network net --ip "172.19.0.40" --name "start3" -p 8080 start run --output report_start_3.json start_3.yml

docker run --network net --ip "172.19.0.40" --name "worload0" -p 8080 start run --output report_workload_0.json workload_0.yml
#docker run --network net --ip "172.19.0.40" --name "worload1" -p 8080 start run --output report_workload_1.json workload_1.yml
#docker run --network net --ip "172.19.0.40" --name "worload2" -p 8080 start run --output report_workload_2.json workload_2.yml
#docker run --network net --ip "172.19.0.40" --name "worload3" -p 8080 start run --output report_workload_3.json workload_3.yml

docker run --network net --ip "172.19.0.40" --name "mine" -p 8080 start run --output report_mine.json mine.yml


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
