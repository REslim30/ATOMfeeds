#!/usr/bin/env bash

# Sends basic HTTP requests and crashes server intentionally.
# Tests whether it handles crashes well.

# Formatting
echo
echo

# Terminates if curl isn't installed
if ! command -v curl &> /dev/null; then
    echo 
    echo "ERROR: curl is not installed. Please install curl."
    echo
    exit
fi


# Testing Helpers
basic_get() {
    curl -s -H "Connection: close" -H "Lamport-Clock: 200" -X GET http://localhost:4567
}

basic_put() {
    curl -s -H "Lamport-Clock: 200" -H "Content-Type: application/atom+xml" -H "Connection: close" -X PUT -d @$(pwd)/src/test/resources/atom/atom_parser/$1 http://localhost:4567/atom.xml
}

initialize_server() {
    make clear_db > /dev/null
    make server url=4567 &> /dev/null &
}

# Gets file specified by $1
# Duplicates with newlines by $2
get_file() {
    fileout=$(cat $(pwd)/src/test/resources/atom/atom_parser/$1)
    for i in $(seq 1 $2); do
        printf "$fileout"
        if let "$i != $2"; then
            printf "\r\n"
        fi
    done
}

test_num=1
testing() {
    echo "${test_num}: testing ${1}"
    let "test_num=test_num+1"
}

assert_equals() {
    if [[ $1 == $2 ]]; then
        echo "TEST PASSED"
    else
        echo "TEST FAILED"

        echo "Expected:   $1"

        echo "But Got:   $2"
    fi
    echo
}

kill_server_instance() {
    echo "Killing Server Instance"
    pkill -P $1
}

kill_server() {
    echo "Killing Server"
    # kill -KILL $1 > /dev/null
    # wait $1 2>/dev/null
    # kill_server_instance $1
    pkill -f server.AggregationServer
}

wait_for_server() {
    echo "Wating for server"
    while [[ -z $(pgrep -f "java -cp target/classes/:src/main/resources:src/test/resources:target/sqlite-jdbc-3.32.3.2.jar:target/mockito-all-1.10.19.jar server.AggregationServer") ]]; do
        sleep 1
    done
}

#TESTS
testing "basic put, get sequence"
initialize_server
last_pid=$!
wait_for_server
basic_put basic.xml > /dev/null
result=$(basic_get)
kill_server $last_pid
assert_equals "$(get_file basic.xml 1)" "$result" 


testing "double put, get sequence"
initialize_server
last_pid=$!
wait_for_server
basic_put basic.xml > /dev/null
basic_put basic.xml > /dev/null
result=$(basic_get)
kill_server $last_pid
assert_equals "$(get_file basic.xml 2)" "$result"

testing "put, crash, get sequence"
initialize_server
last_pid=$!
wait_for_server
kill_server_instance $last_pid
basic_put basic.xml > /dev/null
wait_for_server
result=$(basic_get)
kill_server $last_pid
assert_equals "$(get_file basic.xml 1)" "$result"

testing "put, put, crash, get sequence"
initialize_server
last_pid=$!
wait_for_server
basic_put basic.xml > /dev/null
basic_put basic.xml > /dev/null
kill_server_instance $last_pid
wait_for_server
result=$(basic_get)
kill_server $last_pid
assert_equals "$(get_file basic.xml 2)" "$result"


testing "put, crash, put, crash, get sequence"
initialize_server
last_pid=$!
wait_for_server
basic_put basic.xml > /dev/null
kill_server_instance $last_pid
wait_for_server
basic_put basic.xml > /dev/null
kill_server_instance $last_pid
wait_for_server
result=$(basic_get)
kill_server $last_pid
assert_equals "$(get_file basic.xml 2)" "$result"

testing "put, put, crash, put, put, crash, put, put, crash, get sequence"
initialize_server
last_pid=$!
wait_for_server
basic_put basic.xml > /dev/null
basic_put basic.xml > /dev/null
kill_server_instance $last_pid

wait_for_server
basic_put basic.xml > /dev/null
basic_put basic.xml > /dev/null
kill_server_instance $last_pid

wait_for_server
basic_put basic.xml > /dev/null
basic_put basic.xml > /dev/null
kill_server_instance $last_pid

wait_for_server
result=$(basic_get)
kill_server $last_pid
assert_equals "$(get_file basic.xml 6)" "$result"

