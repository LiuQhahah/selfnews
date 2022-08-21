#!/bin/bash

pid=`ps -ef | grep selfnews-0.0.1-SNAPSHOT.jar  | grep -v 'grep'| awk '{print $2}'`

if [[ $pid ]]
then
  echo 'kill running jar ' $pid
  kill -9 "$pid"
fi
