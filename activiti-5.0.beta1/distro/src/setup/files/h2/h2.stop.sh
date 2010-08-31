#!/bin/sh

java -cp h2-1.2.132.jar org.h2.tools.Server -tcpShutdown tcp://localhost:9092
