#Defines the directory runtime will look for packages and resources

#For convinience
compile=javac -d target
run=java -cp "target:src/main/resources:src/test/resources"


server: compile_server
	$(run) main.java.server.AggregationServer $(port)

compile_server: src/main/java/server/*.java
	$(compile) $?