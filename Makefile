#Defines the directory runtime will look for packages and resources
#For convinience
class_flag=-cp "target/classes/:src/main/resources:src/test/resources:target/sqlite-jdbc-3.32.3.2.jar:target/mockito-all-1.10.19.jar"
compile=javac $(class_flag) -d target/classes/
run=java $(class_flag)

#*** Main Processes ***
#ContentServer
content: compile_content
ifeq ($(and $(url),$(file)),)
	@echo
	@echo "Usage: make content url=<url> file=<file_name>"
	@echo "<url>: <host_name>:<port_number>"
	@echo
else
	@$(run) content.ContentServer $(url) $(file)
endif

#GETClient
client: compile_client
ifeq ($(url),)
	@echo
	@echo "Usage: make client url=<url>"
	@echo "<url>: <host_name>:<port_number>"
	@echo
else
	@$(run) client.GETClient $(url)
endif

#AggregationServer
server: compile_server
ifeq ($(port),)
	@echo
	@echo "Usage: make server port=<port_number>"
	@echo
endif
	@until $(run) server.AggregationServer $(port); do \
		echo "AggregationServer crashed with exit code $$?. Respawning.." >&2 ; \
		sleep 1; \
	done



#*** Tests ***
test_class_flag=-cp "target/classes/:target/test-classes:src/test/resources:target/junit-4.13.jar:target/hamcrest-core-1.3.jar:target/sqlite-jdbc-3.32.3.2.jar:target/mockito-all-1.10.19.jar"
compile_test=javac $(test_class_flag) -d target/test-classes/
run_test=java $(test_class_flag) org.junit.runner.JUnitCore

test_http: compile_test_http
	@$(run_test) http.URLParserTest
	@$(run_test) http.HTTPResponseReaderTest
	@$(run_test) http.HTTPRequestReaderTest
	@$(run_test) http.HTTPResponseWriterTest

test_server: compile_test_server
	@$(run_test) server.AggregationStorageManagerTest
	@$(run_test) server.LamportClockTest
	@$(run_test) server.AggregationResponderThreadTest

test_slow_server: compile_test_server
	@$(run_test) server.AggregationStorageManagerSlowTest
	@$(run_test) server.AggregationDeleterThreadTest

test_atom: compile_test_atom
	@$(run_test) atom.TextToAtomParserTest
	@$(run_test) atom.AtomParserTest

test_client: compile_test_client
	@$(run_test) client.GETClientTest

test_content: compile_test_content
	@$(run_test) content.ContentServerTest

#***Test Compliation***
compile_test_http: compile_http src/test/java/http/*.java
	@$(compile_test) src/test/java/http/*.java

compile_test_server: compile_server src/test/java/server/*.java
	@$(compile_test) src/test/java/server/*.java

compile_test_atom: compile_atom src/test/java/atom/*.java
	@$(compile_test) src/test/java/atom/*.java 

compile_test_client: compile_client src/test/java/client/*.java
	@$(compile_test) src/test/java/client/*.java

compile_test_content: compile_content src/test/java/content/*.java
	@$(compile_test) src/test/java/content/*.java

#***Compliation***
compile_client: src/main/java/client/*java compile_http compile_atom
	@$(compile) src/main/java/client/*java

compile_content: src/main/java/content/*java compile_http compile_atom
	@$(compile) src/main/java/content/*java

compile_server: src/main/java/server/*.java compile_http compile_atom
	@$(compile) src/main/java/server/*java

#HTTP helpers
compile_http: src/main/java/http/*.java
	@$(compile) $?

#Atom helpers
compile_atom: src/main/java/atom/*.java
	@$(compile) $?


#Zips relevant files
zip: clean
	zip -r project.zip Makefile README.md designs src target

clean:
	rm -r --force target/classes/*
	rm -r --force target/test-classes/*
