#!/bin/bash
export set JAVA_OPTS="-XX:+AggressiveHeap"
export CLASSPATH="src:bin:lib/commons-math3-3.6/*"
#find . -name "*.java" -print | xargs javac
javac src/*.java 
java NetworkStatesWrapper 0 500
#java NetworkStatesWrapper 5000 250
#java NetworkStatesWrapper 10000 500
#java NetworkStatesWrapper 15000 500
