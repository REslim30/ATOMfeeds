package http;

import java.io.*;
import java.util.*;
import java.lang.StringBuilder;

//Reads in a single HTTP request
public class HTTPRequestReader {
    private BufferedReader in;
    
    //HTTP request fields
    private String requestLine[];
    private HashMap<String, String> headers;
    private String body;

    public HTTPRequestReader(BufferedReader in) {
        this.in = in;
        headers = new HashMap<String, String>();
    }

    //Reads the request
    public void readRequest() throws IOException {
        parseRequestLine();
        parseHeaders();
        parseBody();
    }

    //Parses the request line of the request
    //Throws an IOException in a bad request
    private void parseRequestLine() throws IOException {
        String inputLine = in.readLine();
        if (inputLine == null) 
            throw new IOException("Seem to have lost connection");

        requestLine = inputLine.split(" ");
        if (requestLine.length != 3) 
            throw new IOException("Request line should have 3 segments");

        if (!requestLine[2].matches("^HTTP.*"))
            throw new IOException("Unkown protocol: " + requestLine[2]);
    }

    //Parses the header fields of the response
    //Assumes headers are delimited by ": "
    //Is case insensitive
    private void parseHeaders() throws IOException {
        String inputLine;
        while ((inputLine = in.readLine()) != null && !inputLine.isEmpty()) {
            int delim = inputLine.indexOf(": ");
            headers.put(inputLine.substring(0, delim).toLowerCase(), inputLine.substring(delim + 2));
        }
        if (inputLine == null) 
            throw new IOException("Seem to have lost connection");
    }

    //Parses the body fields of the response
    //Assumes text based
    private void parseBody() throws IOException {
        int size = Integer.parseInt(headers.getOrDefault("content-length", "0"));

        char[] bodyChars = new char[size];
        if (in.read(bodyChars, 0, size) < size) 
            throw new IOException("Body ended before full content-length characters");
        
        body = new String(bodyChars);
    }

    //Returns a string representation of the response
    public String toString() {
        if (requestLine == null || headers == null)
            throw new RuntimeException("HTTPRequestReader: toString() but response and headers are null.");

        StringBuilder response = new StringBuilder();
        response.append(requestLine[0] + ' ' + requestLine[1] + ' ' + requestLine[2] + '\n'); 
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            response.append(entry.getKey() + ": " + entry.getValue() + "\n");
        }
        response.append("\nBody:\n");
        response.append(body);
        return response.toString();
    }

    //Returns the current status code.
    public String getMethod() {
        if (requestLine == null) 
            throw new RuntimeException("HTTPRequestReader: tried to getMethod but requestLine is null");

        return requestLine[0];
    }

    //Returns the current resource URL
    public String getURL() {
        if (requestLine == null) 
            throw new RuntimeException("HTTPRequestReader: tried to getURL but no requestLine is null");
        
        return requestLine[1];
    }

    //Returns the body of the message
    public String getBody() {
        if (body == null) 
            throw new RuntimeException("Tried to getBody but body is null");
        
        return body;
    }

    //Returns a header field
    //Returns null if unavailable
    public String getHeader(String input) {
        return headers.getOrDefault(input.toLowerCase(), null);
    }
}
