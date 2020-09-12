package main.java.server;

import java.net.*;
import java.io.*;
import main.java.http.HTTPRequestReader;
//import java.util.concurrent.*;

/**
 * AggregationResponderThread
 * Listens and responds to a single client
 */
public class AggregationResponderThread extends Thread {
    //needed to handle lamport clocks that are of equal value
    private long connectionId;
    private Socket socket;

    public AggregationResponderThread(Socket socket , long connectionId) {
        this.socket = socket;
        this.connectionId = connectionId;
    }
    
    public void run() {
        System.err.println("AggregationResponderThread has started");
        //Reads the request
        HTTPRequestReader request = null;
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream()));
        ) {
            request = new HTTPRequestReader(in);
            request.readRequest();
        } catch (IOException e) {
            System.err.println("AggregationResponderThread: Error while reading in request: " + e.toString());
        }

        //If there's no lamport clock value, send a bad request message
        if (request.getHeader("lamport-clock") == null) {
            //TODO - Send a bad request message
            System.err.println("Request had no lamport-clock ending connection");
            System.exit(1);
        }

        //Handle GET and PUT requests
        if (request.getMethod() == "GET") {
            handleGET(request);
        } else if (request.getMethod() == "PUT")  {
            handlePUT(request);
        } else {
            //TODO - Send a method unimplemented
            System.err.println("Method unimplemented");
            System.exit(0);
        }
        
        System.out.println("AggregationResponderThread has ended");
    }

    private void handleGET(HTTPRequestReader request) {
        
    }

    private void handlePUT(HTTPRequestReader request) {
        
    }
}
