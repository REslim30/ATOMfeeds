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
    make server url=4567 & > /dev/null
}

get_file() {
    cat $(pwd)/src/test/resources/atom/atom_parser/$1
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

        echo "But Got: $2"
    fi
    echo
}

kill_process() {
    kill -KILL $1 > /dev/null
    wait $1 2>/dev/null
}

# TESTS
testing "basic put, get sequence"
initialize_server
last_pid=$!
sleep 1
basic_put basic.xml > /dev/null
result=$(basic_get)
kill_process $last_pid
assert_equals "$result" "$(get_file basic.xml)"


testing "double put, get sequence"
initialize_server
last_pid=$!
sleep 1
basic_put basic.xml > /dev/null
basic_put basic.xml > /dev/null
result=$(basic_get)
kill_process $last_pid
assert_equals "$result" "$(get_file basic.xml)"


# Some formatting issues with make
echo
echo


