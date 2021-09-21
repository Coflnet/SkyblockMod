#! /bin/bash
mkdir /app
echo "Preparing to Copy"
shopt -s extglob
cp -r /data/!(run|eclipse|mock) /app
echo "Done Copying"
cd /app/ || exit
ls -l
echo "Beginning Build"
./gradlew build
echo "Build finished"
ls /app/build/libs
cp /app/build/libs/* /artifacts/