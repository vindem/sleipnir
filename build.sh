#!/usr/bin/env sh

mvn clean package -Dmaven.test.skip
spark-submit ./target/sleipnir.jar
