#Defines the directory runtime will look for packages and resources

#For convinience
class_flag=-cp "target:src/main/resources:src/test/resources"
compile=javac $(class_flag) -d target
run=java $(class_flag)

client: compile_client
	$(run) main.java.client.GETClient $(host) $(port)

compile_client: src/main/java/client/*java compile_http
	$(compile) src/main/java/client/*java


server: compile_server
	$(run) main.java.server.AggregationServer $(port)

compile_server: src/main/java/server/*.java
	$(compile) $?


compile_http: src/main/java/http/*.java
	$(compile) $?