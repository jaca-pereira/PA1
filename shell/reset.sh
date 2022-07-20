#!/bin/bash
docker rm $(docker stop redis_g2)
docker rm $(docker stop replica_g2)
docker rm $(docker stop proxy_g2)