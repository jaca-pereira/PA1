#!/bin/bash

cd ../replica

mvn clean compile assembly:single

docker build -t replica .

docker run -t replica

