.PHONY: all clean build test test-momento test-redis format lint precommit help


all: precommit

## Clean the project
clean:
	./gradlew clean

## Build the project
build:
	./gradlew assemble

## Build the examples
build-examples:
	cd ./examples && ./gradlew assemble

## Run all the tests
test: test-momento test-redis

## Run the tests vs Momento
test-momento:
	./gradlew test

## Run the tests vs Redis
test-redis:
	REDIS=1 ./gradlew test

## Format the code
format:
	./gradlew spotlessApply

## Lint the code
lint:
	./gradlew spotlessCheck

## Run the precommit checks
precommit: format lint build test

# See <https://gist.github.com/klmr/575726c7e05d8780505a> for explanation.
help:
	@echo "$$(tput bold)Available rules:$$(tput sgr0)";echo;sed -ne"/^## /{h;s/.*//;:d" -e"H;n;s/^## //;td" -e"s/:.*//;G;s/\\n## /---/;s/\\n/ /g;p;}" ${MAKEFILE_LIST}|LC_ALL='C' sort -f|awk -F --- -v n=$$(tput cols) -v i=19 -v a="$$(tput setaf 6)" -v z="$$(tput sgr0)" '{printf"%s%*s%s ",a,-i,$$1,z;m=split($$2,w," ");l=n-i;for(j=1;j<=m;j++){l-=length(w[j])+1;if(l<= 0){l=n-i-length(w[j])-1;printf"\n%*s ",-i," ";}printf"%s ",w[j];}printf"\n";}'|more $(shell test $(shell uname) == Darwin && echo '-Xr')
