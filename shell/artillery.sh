#!/bin/bash

cd ../artillery

docker run --rm -it -v ${PWD}:/scripts \
  artilleryio/artillery:latest \
  run /scripts/create_accounts.yaml \
  run /scripts/workload_bftsmart.yaml \
  run /scripts/workload_mining.yaml
  
cd ../shell
