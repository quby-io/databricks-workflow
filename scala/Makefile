assembly: ##           packages scala code with dependencies from the this project
	sbt assembly

package: ##            packages scala code from the this project
	sbt package

test: ##               runs all the unit test
	sbt test

it_test: ##            runs integration tests
	sbt it:test

help: ##               show this help.
	@fgrep -h "##" $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/##//'