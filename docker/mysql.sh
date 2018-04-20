#!/usr/bin/env bash

case "$1" in
    "start") docker run -p 33066:3306 --name mysql-nxsecfp -e 'MYSQL_USER=mysql' -e 'MYSQL_PASSWORD=mysql' -e 'MYSQL_DATABASE=nxsecfp' -e 'MYSQL_RANDOM_ROOT_PASSWORD=yes' -d mariadb:10.0 ;;
     "stop") docker stop mysql-nxsecfp && docker rm mysql-nxsecfp ;;
          *) echo Unknown command: $1 ;;
esac