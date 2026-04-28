#!/bin/bash
echo "College Event Management System"
mkdir -p out
find src -name "*.java" | xargs javac -d out -sourcepath src
if [ $? -ne 0 ]; then echo "Compilation failed."; exit 1; fi
java -cp out Main
