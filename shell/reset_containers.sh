#!/bin/sh
docker rm $(docker stop $(docker ps -a -q ))

rm -r ../security
rm -r ../client/security
rm -r ../proxy/security
mkdir ../security
mkdir ../client/security
mkdir ../proxy/security

rm ../replica/config/currentView
rm ../proxy/config/currentView

docker network remove net


