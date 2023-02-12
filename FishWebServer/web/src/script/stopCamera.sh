#!/bin/bash

PID_FILE_SERVER="exe/pid-server"
PID_FILE_CAMERA="exe/pid-camera"

if [ -f "$PID_FILE_SERVER" ]; then
  pid=$(cat $PID_FILE_SERVER)
  echo "Stopping Camera-server on $pid"
  kill -9 $pid
  rm $PID_FILE_SERVER

else
  echo "Camera-server is not running. If you sure server is running, delete $PID_FILE_SERVER manually"
fi

if [ -f "$PID_FILE_CAMERA" ]; then
  pid=$(cat $PID_FILE_CAMERA)
  echo "Stopping Camera on $pid"
  kill -9 $pid
  rm $PID_FILE_CAMERA
else
  echo "Camera is not running. If you sure server is running, delete $PID_FILE_CAMERA manually"
fi