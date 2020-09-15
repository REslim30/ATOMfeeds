
Notes:
- HTTP/1.1 (RFC 7230)
    - Two Options: Non-persistent
    - Array of strings.
    - Handles concurrent requests

### Notes on external libraries
    - I used the following external libraries:
        - Junit -> for unit tests.
        - sqlite-jdbc -> for maintaining feeds.
    - Please let me know if these are not allowed.


### Notes on Running
    - All processes should be run from the main parent directory. (Where this README.md is located)


### Note on lamport clocks
    - lamport clocks on the server are updated for:
        - receiving requests.
        - sending requests.
        - aggregating feeds.
        - saving feeds.
    - All requests are responded to in order of the request being "received". Note: the server 
    only consideres a request to be "received" when the server has verified the following conditions: 
        - the request includes a valid lamport-clock in its header.
        - Is a valid method (e.g. GET and PUT). 
        - It accesses a valid resource.
        - If it is a PUT request, that it is a valid Atom document.
        
    Once the server has verified these conditions are true, the server considers the request as received.
    The server will then respond to the requests in this order. This defintion of "received" was chosen over the strict
    definition of received where a request is considered received the very first bit has arrived to reduce the
    average latency of requests (as it allows for more parallel processing) and also for a sense of fairness. 
    E.g. it would be wasteful and unfair if a valid GET request had to wait for the server to finish verifying and 
    parsing an invalid PUT request. 

    In essence, the server ensures logical ordering amongst valid requests, but 
    handles invalid requests concurrently. Please let me know if this is unacceptable.

### Notes on testing
    - Unit tests were created.
        - The server unit tests were separated into two tests. normal tests and slow tests. 
        Slow tests being tests that require explicit waiting.

        make test_server  -> runs normal tests.
        make test_slow_server   -> runs slow tests. 
