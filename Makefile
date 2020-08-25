#Defines the directory runtime will look for packages and resources
flags=-cp "target:src/main/resources:src/test/resources"

#For convinience
compile=javac $(flags)
run=java $(flags)

all:
	@echo $(run)