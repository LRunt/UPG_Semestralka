#!/bin/bash
javadoc -encoding UTF-8 -sourcepath ./src -cp ./src -d doc/javadoc -version -author ./src/*.java
