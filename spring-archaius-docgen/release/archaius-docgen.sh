#!/bin/bash

if ! command -v java &> /dev/null
then
    echo "Java is NOT installed."
    exit 1
else
    echo "Java is installed."
fi

java -jar ../lib/spring-archaius-docgen.jar "$@"