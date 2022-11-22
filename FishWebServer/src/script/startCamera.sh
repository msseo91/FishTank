#!/bin/bash

FISH_DIR="$HOME/fishtank"
PID_FILE_SERVER="$FISH_DIR/exe/pid-server"
PID_FILE_CAMERA="$FISH_DIR/exe/pid-camera"

datePrefix=`date +%y-%m-%d`
LOG_FILE_RTSP="$FISH_DIR/log/rtsp-$datePrefix.log"
LOG_FILE="$FISH_DIR/log/camera-$datePrefix.log"

USER_NAME="msseo"
PASSWORD="1116"

cd ~/rtsp
./rtsp-simple-server > $LOG_FILE_RTSP &

echo "RTSP pid=$!"
echo $! > $PID_FILE_SERVER

sleep 2

libcamera-vid --width 1920 --height 1080 -n -t 0 --inline -o - | ffmpeg -re -i pipe:0 -c:v copy -f rtsp -muxdelay 0.1 "rtsp://${USER_NAME}:${PASSWORD}@localhost:8888/fishtank" &> $LOG_FILE &

echo $! > $PID_FILE_CAMERA
