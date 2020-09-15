package main.java.http;

import java.io.*;
import java.util.*;
import java.lang.StringBuilder;

//Reads in a single HTTP response
public class HTTPResponseReader {
    private BufferedReader in;

    //HTTP response fields
    private String statusLine[];
    private HashMap<String, String> headers;
    private String body;
    public HTTPResponseReader(BufferedReader in) {
        this.in = in;
        headers = new HashMap<String, String>();
    }
    public void readResponse() throws IOException {
        parseStatusLine();
        parseHeaders();
        parseBody();
    }

    //Parses the status line of the response
    //Throws an IOException in a bad response
    private void parseStatusLine() throws IOException {
        String inputLine = in.readLine();
        if (inputLine == null)
            throw new IOException("Seem to have lost connection");

        statusLine = new String[3];
        
        System.err.println(inputLine);

        //Parses StatusLine
        int prevSpace = 0;
        int curSpace = inputLine.indexOf(' ');
        if (curSpace == -1) 
            throw new IOException("Unkown Status Line: " + inputLine);
        
        statusLine[0] = inputLine.substring(prevSpace, curSpace);

        prevSpace = curSpace + 1;
        curSpace = inputLine.indexOf(' ', prevSpace);
        if (curSpace == -1) 
            throw new IOException("Unkown Status Line: " + inputLine);

        statusLine[1] = inputLine.substring(prevSpace, curSpace);
        statusLine[2] = inputLine.substring(curSpace+1);


        if (!statusLine[0].matches("^HTTP.*")) 
            throw new IOException("Unknown protocol:" + statusLine[0]);

        if (!statusLine[1].matches("\\d\\d\\d"))
            throw new IOException("Unknown status code: " + statusLine[1]);
        
    }

    //Parses the header fields of the response
    //Assumes headers are delimited by ': '
    private void parseHeaders() throws IOException {
        String inputLine;
        while ((inputLine = in.readLine()) != null && !inputLine.isEmpty()) {
            int delim = inputLine.indexOf(": ");
            headers.put(inputLine.substring(0, delim).toLowerCase(), inputLine.substring(delim+2));
        }
        if (inputLine == null)
            throw new IOException("Seem to have lost connection");
    }

    //Parses the body fields of the response
    //Does not support anything other than text
    //Or application/xml+atom
    private void parseBody() throws IOException {
        int size = Integer.parseInt(headers.getOrDefault("content-length", "0"));
        
        char[] charBuf = new char[size];
        if (in.read(charBuf, 0, size) < size) {
            throw new IOException("Seem to have lost connection");
        }
        body = new String(charBuf);
    }

    //Returns a string representation of the response
    public String toString() {
        StringBuilder response = new StringBuilder();
        response.append(statusLine[0] + ' ' + statusLine[1] + ' ' + statusLine[2] + '\n'); 
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            response.append(entry.getKey() + ": " + entry.getValue() + "\n");
        }
        response.append("\nBody:\n");
        response.append(body);
        return response.toString();
    }

    //Returns the current status code.
    public int getStatusCode() {
        if (statusLine == null || statusLine.length != 3) 
            throw new RuntimeException("HTTPResponseReader: tried to getStatusCode but no statusLine available");

        return Integer.parseInt(statusLine[1]);
    }

    //Returns the current status message.
    public String getStatusMsg() {
        if (statusLine == null) 
            throw new RuntimeException("Tried to getStatusMsg but invalid statusLine");
        
        return statusLine[2];
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
