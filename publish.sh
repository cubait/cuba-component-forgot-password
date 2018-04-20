#!/usr/bin/env bash

[[ -z $1 ]] && echo -e "\e[91mMissing required argument: BinTray API Key\e[0m" && exit 1

export UPLOAD_REPOSITORY_USERNAME=pfurini UPLOAD_REPOSITORY_PASSWORD=$1
./gradlew uploadArchives
