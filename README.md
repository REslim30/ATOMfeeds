# Assignment 2
###### By a1742494 - Huy Gia Do Vu
An Atom syndication system communicating between eachother using HTTP/1.1 (RFC 7230)
persistent connections that support a subset of Atom elements (defined as per RFC-4287 and
the assignment specification). Written in Java 8.

*The following document has been written in markdown if that helps*

### How to run
All processes should be run from the main parent directory (Where this README.md is located) 
using the provided make commands:
    
    make server port=<port_number>              Starts the aggregation server on port <port_number>
    make content url=<url> file=<file_name>     Starts the content server, to send a PUT request to <url> with file <file_name>
    make client url=<url>                       Starts the GET client, to send a GET request to <url>

If you wanted to add extra files for the Content server add a text based file to:

    src/main/resources/content

This project also includes tests:

    make test_server                            Runs aggregation server tests. 
    make test_slow_server                       Runs aggregation server tests that require explicit waiting.
    make test_http                              Runs tests that involve HTTP helpers.
    make test_atom                              Runs tests that involve Atom helpers.

If you wanted to see these test cases, see:

    src/test/java

Some tests read in files from:

    src/test/resources

### Adding Files
The following project follows the Maven project directory structure:
    
    src        -> source files & resources.
    target     -> build folder (.class)

## Notes
#### Notes on external libraries
I used the following external libraries:
    
    Junit          -> for unit tests.
    sqlite-jdbc    -> for maintaining feeds.

#### Note on lamport clocks
lamport clocks on the server are updated for:
- receiving requests.
- sending requests.
- aggregating feeds.
- saving feeds.


All requests are responded to in order of the request being "received". Note: the server only consideres a 
request to be "received" when the server has verified the following conditions: 
    
   - the request includes a valid lamport-clock in its header.
   - Is a valid method (e.g. GET and PUT). 
   - It accesses a valid resource.
   - If it is a PUT request, that it is a valid Atom document.
        

Once the server has verified these conditions are true, the server considers the request as received.
The server will then respond to the requests in this order. This is opposed to considering the request as received
as soon as the very first bit of the request has arrived. 

This was a trade of made to reduce the average latency of requests (as it allows for more parallel processing) and also for a sense of fairness. 
It would be wasteful and unfair if a valid GET request had to wait for the server to finish parsing and responding to an invalid PUT request. 

In essence, the server ensures logical ordering amongst valid requests, but 
handles invalid requests in any order. Please let me know if this is unacceptable.


#### Contact Me
Thanks for looking through my assignment. You can contact me through uni emails or giahuydo99@gmail.com
