#!/bin/sh

mvn -Ddatabase=$1 -Pcheck clean install
