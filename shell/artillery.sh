#!/bin/bash

cd ../artillery

docker run --rm -it -v ${PWD}:/scripts \
  artilleryio/artillery:latest \
  run /scripts/create_accounts.yaml \
  
cd ../shell
