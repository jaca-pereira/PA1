#!/bin/bash

cd ../artillery


#artillery run --output report_start_0.json start_0.yml
#artillery run --output report_start_1.json start_1.yml
#artillery run --output report_start_2.json start_2.yml
#artillery run --output report_start_3.json start_3.yml

#artillery run --output report_workload_0.json workload_0.yml
#artillery run --output report_workload_1.json workload_1.yml
#artillery run --output report_workload_2.json workload_2.yml
#artillery run --output report_workload_3.json workload_3.yml

artillery run --output report_mine_0.json mine_0.yml

#artillery report report_start_0.json
#artillery report report_start_1.json
#artillery report report_start_2.json
#artillery report report_start_3.json

#artillery report report_workload_0.json
#artillery report report_workload_1.json
#artillery report report_workload_2.json
#artillery report report_workload_3.json

artillery report report_mine.json


cd ../shell
