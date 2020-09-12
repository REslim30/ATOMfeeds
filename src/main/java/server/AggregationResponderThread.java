package main.java.server;

import java.net.*;
import java.io.*;
import main.java.http.HTTPRequestReader;
import main.java.http.HTTPResponseWriter;
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
        HTTPRequestReader reader = null;
        HTTPResponseWriter writer = null;
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());
        ) {
            reader = new HTTPRequestReader(in);
            reader.readRequest();
            writer = new HTTPResponseWriter(out);

            respond(reader, writer);
        } catch (IOException e) {
            System.err.println("AggregationResponderThread: Error while reading in request: " + e.toString());
        }

        
        System.out.println("AggregationResponderThread has ended");
    }

    //Responds to peer depending on GET or PUT request
    private void respond(HTTPRequestReader reader, HTTPResponseWriter writer) {
        //Return bad response if unimplemented methods
        if (!reader.getMethod().equals("GET") && !reader.getMethod().equals("PUT")) {
            writer.writeResponse(400, "Only GET and PUT is implemented on this server");
            System.out.println("Received unimplmented method.");
            return;
        }

        //If there's no lamport clock value, send a bad request message
        if (reader.getHeader("lamport-clock") == null) {
            writer.writeResponse(400, "You must include a lamport-clock header inside your request");
            System.out.println("Received message without lamport-clock value");
            return;
        }

        //Handle GET and PUT requests
        if (reader.getMethod().equals("GET")) {
            handleGET(reader, writer);
        } else {
            handlePUT(reader, writer);
        }     

    }
    

    //Sends a response for a GET request
    private void handleGET(HTTPRequestReader reader, HTTPResponseWriter writer) {
        if (reader.getURL().equals("/")) {
            writer.writeResponse(404, "GET resource does not exist. Only resource available is '/'");
            return;
        }

        //TODO: Create retrieval method
        writer.writeResponse(200, "looks okay.");
    }

    //Sends a response for a PUT request
    private void handlePUT(HTTPRequestReader reader, HTTPResponseWriter writer) {
        if (reader.getURL().equals("/atom.xml")) {
            writer.writeResponse(404, "PUT resource does not exist. Only resource available is '/atom.xml'");
            return;
        }

        if (reader.getHeader("Content-Length") == null) {
            writer.writeResponse(411, "Content-Length header missing.");
            return;
        }

        if (reader.getHeader("Content-Length").equals("0")) {
            writer.writeResponse(204, "Body is empty. Did nothing");
            return;
        }

        //TODO: Create saving method
        writer.writeResponse(200, "looks okay.");
    }
}
