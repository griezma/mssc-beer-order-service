#!/bin/bash

service_name=beerorder-service
pw=letmein
port=3307

container=mysql-$service_name
volume=${service_name}-data

docker stop $container

docker volume rm -f $volume

docker volume create $volume

docker run --rm --name $container \
  -v ${volume}:/var/lib/mysql \
  -e MYSQL_ROOT_PASSWORD=$pw \
  -e MYSQL_DATABASE=$service_name \
  -e MYSQL_USER=$service_name \
  -e MYSQL_PASSWORD=$pw \
  -p ${port}:3306 \
  -d mysql