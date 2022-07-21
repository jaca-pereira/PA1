#!/bin/bash

cd ../artillery

if [ $# -eq 0 ] ; then
    echo "Usage: sh artillery.sh <id>"
    exit 1
fi

ID=$1

artillery run --output report_start_$ID.json start_$ID.yml

artillery run --output report_workload_$ID.json workload_$ID.yml


artillery report report_start_$ID.json

artillery report report_workload_$ID.json



cd ../shell
