#! /bin/bash
mkdir /tmp/app
echo "Preparing to Copy"
shopt -s extglob
cp -r /data/!(run|eclipse|mock) /tmp/app
echo "Done Copying"
cd /tmp/app/ || exit
ls -l
echo "Beginning Build"
./gradlew build
echo "Build finished"
ls /tmp/app/build/libs
cp /tmp/app/build/libs/* /artifacts/