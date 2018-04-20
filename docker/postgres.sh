#!/usr/bin/env bash

case "$1" in
    "start") docker run -p 54322:5432 --name postgres-nxsecfp -e 'POSTGRES_USER=postgres' -e 'POSTGRES_PASSWORD=postgres' -e 'POSTGRES_DB=nxsecfp' -d postgres:9-alpine ;;
     "stop") docker stop postgres-nxsecfp && docker rm postgres-nxsecfp ;;
          *) echo Unknown command: $1 ;;
esac