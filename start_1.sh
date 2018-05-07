#!/bin/bash
export set JAVA_OPTS="-XX:+AggressiveHeap"
export CLASSPATH="src:bin:lib/*"
#find . -name "*.java" -print | xargs javac
javac src/*.java 
java NetworkStatesWrapper 7
