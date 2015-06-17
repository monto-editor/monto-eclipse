#!/bin/sh

java -jar lib/antlr-4.4-complete.jar \
	-package de.tudarmstadt.stg.monto.token.java8 \
	-no-listener \
	-no-visitor \
	src/de/tudarmstadt/stg/monto/token/java8/Java8.g4
