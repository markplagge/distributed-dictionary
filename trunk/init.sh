#!/bin/env bash

#Please look at hosts.dat file to know the hostnames of the IPs used below

router_ip="128.111.43.36"

if [ "$1" == "r" ]
then
 java -cp lib/log4j-1.2.16.jar:build/classes router.Router 128.111.43.54:128.111.43.42:128.111.43.55:128.111.43.26:128.111.43.30
elif [ "$1" == "0" ] || [ "$1" == "1" ] || [ "$1" == "2" ] || [ "$1" == "3" ] || [ "$1" == "4" ]
then
 java -cp lib/log4j-1.2.16.jar:build/classes client.Client $router_ip 9999 $1
fi