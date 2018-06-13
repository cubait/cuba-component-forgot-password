#!/usr/bin/env bash

usage()
{
    echo
    echo "usage: ./publish.sh <bintray_api_key> [version] [additional_bintray_params]"
    echo
    echo "Publish the package to Bintray, optionally specifying an artifact version."
    echo "If [version] is not specified, the latest version is published."
    echo
}

if [[ -z "$BINTRAY_API_KEY" ]]; then
    [[ -z "$1" ]] && usage && exit 1
    BINTRAY_API_KEY=$1
    shift
fi

if [[ -n "$1" ]] && [[ ! $1 == -P* ]]; then
    __VERSION=-Pcuba.artifact.version=$1
    shift
fi

./gradlew clean assemble bintrayUpload -PbintrayUser=pfurini -PbintrayApiKey=$BINTRAY_API_KEY $__VERSION $@