#!/bin/bash

cd ../artillery


artillery run --output report_start_0.json start_0.yml

artillery run --output report_workload_0.json workload_0.yml


artillery report report_start_0.json

artillery report report_workload_0.json



cd ../shell
