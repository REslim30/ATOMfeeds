#Defines the directory runtime will look for packages and resources

#For convinience
class_flag=-cp "target:src/main/resources:src/test/resources"
compile=javac $(class_flag) -d target
run=java $(class_flag)

#ContentServer
content: compile_content
ifeq ($(and $(url),$(file)),)
	@echo
	@echo "Usage: make content url=<url> file=<file_name>"
	@echo "<url>: <host_name>:<port_number>"
	@echo
else
	@$(run) main.java.content.ContentServer $(url) $(file)
endif

#GETClient
client: compile_client
ifeq ($(url),)
	@echo
	@echo "Usage: make client url=<url>"
	@echo "<url>: <host_name>:<port_number>"
	@echo
else
	$(run) main.java.client.GETClient $(url)
endif

#AggregationServer
server: compile_server
ifeq ($(port),)
	@echo
	@echo "Usage: make server port=<port_number>"
	@echo "if <port_number> is left empty, then port is set to 4567"
	@echo
endif
	@$(run) main.java.server.AggregationServer $(port)


#***Compliation
compile_client: src/main/java/client/*java compile_http
	@$(compile) src/main/java/client/*java

compile_content: src/main/java/content/*java compile_http
	@$(compile) src/main/java/content/*java

compile_server: src/main/java/server/*.java compile_http
	@$(compile) src/main/java/server/*java

#HTTP helpers
compile_http: src/main/java/http/*.java
	@$(compile) $?
