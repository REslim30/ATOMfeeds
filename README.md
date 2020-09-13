
Notes:
- HTTP/1.1 (RFC 7230)
    - Two Options: Non-persistent
    - Array of strings.
    - Handles concurrent requests

### Notes on Running
    - All processes should be run from the main parent directory. (Where this README.md is located)

### Note on lamport clocks
    - lamport clocks on the server are updated for:
        - receiving requests.
        - sending requests.
        - aggregating feeds.
        - saving feeds.


### File System
    - ATOM files are stored on the system are named in a special format:
        <lamport-clock>_<connection-id>.xml
    where <lamport-clock> is the lamport clock value of the PUT request,
    and <connection-id> is the connection_id of the connection. This allows
    the server to maintain logical ordering
    
