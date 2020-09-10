
Notes:
- HTTP/1.1 (RFC 7230)
    - Two Options: Non-persistent
    - Array of strings.
    - Handles concurrent requests


*** Notes on Lamport Clocks ***
    - In order to choose between equal lamport clock values
    the aggregation server assigns each connection a connection_id.
    In a real life system, this would be impractical as we would
    run out of available id's over time. However, since other methods
    like using a IP-PID combo are just a bit more complicated to use,
    I've decided that these other methods are a little out of scope
    for this assignment.

*** File System ***
    - ATOM files are stored on the system are named in a special format:
        <lamport-clock>_<connection-id>.txt
    where <lamport-clock> is the lamport clock value of the PUT request,
    and <connection-id> is the connection_id of the connection. This allows
    the server maintain logical ordering. 
    
