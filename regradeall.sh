#!/bin/bash

FILE=$1

while IFS= read -ru 3 LINE; do
    curl -X POST --header "Content-Type: application/json" https://circleci.com/api/v1/project/cs3134/homework-$LINE/tree/master?circle-token=22174cf419b2a768da829f493092204e3deba111
done 3< "$FILE"
