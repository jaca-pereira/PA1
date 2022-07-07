#!/bin/sh
docker rm $(docker stop $(docker ps -a -q ))

rm ../replica/config/currentView
rm ../proxy/config/currentView



