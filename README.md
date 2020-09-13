
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
    only consideres a request to be "received" when the server has verified that the request includes
    a valid lamport-clock in its header and is a valid method (e.g. GET and PUT). This does mean that
    requests may arrive in a certain order, but be considered "received" in another order.
    The server guarantees that requests are responded to in the order that it was received.

