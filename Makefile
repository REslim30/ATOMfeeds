#Defines the directory runtime will look for packages and resources

#For convinience
compile=javac -d target
run=java -cp "target:src/main/resources:src/test/resources"

client: compile_client
	$(run) main.java.client.GETClient localhost 3000

compile_client: src/main/java/client/*java 
	$(compile) $?


server: compile_server
	$(run) main.java.server.AggregationServer $(port)

compile_server: src/main/java/server/*.java
	$(compile) $?