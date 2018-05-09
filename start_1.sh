#!/bin/bash
export set JAVA_OPTS="-XX:+AggressiveHeap"
export CLASSPATH="src:bin:lib/commons-math3-3.6/*"
#find . -name "*.java" -print | xargs javac
javac src/*.java 
java NetworkStatesWrapper 500 15000 9
java NetworkStatesWrapper 500 15000 5
java NetworkStatesWrapper 500 15000 2