#!/bin/bash
sh reset_containers.sh
sh network.sh
sh replica.sh
sh proxy.sh
sh client.sh
sh artillery.sh
