#!/bin/bash

if [ $# -eq 0 ]; then
    echo "Usage: deploy_compose.sh <blockmess>"
    exit 1
fi

cd ../compose
docker compose up
