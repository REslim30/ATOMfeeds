package main.java.server;

import java.net.*;
import java.io.*;
import java.util.function.*;
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
    private LamportClock lamportClock;
    private Socket socket;

    public AggregationResponderThread(Socket socket , long connectionId, LamportClock lamportClock) {
        this.socket = socket;
        this.connectionId = connectionId;
        this.lamportClock = lamportClock;
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
            while (true) {
                reader = new HTTPRequestReader(in);
                reader.readRequest();
                writer = new HTTPResponseWriter(out);

                respond(reader, writer);
                //end connection if client wants to end connection
                if (reader.getHeader("connection") != null && reader.getHeader("connection").equals("close"))
                    break;
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("AggregationResponderThread: Error while managing connection: " + e.toString());
        }

        
        System.out.println("AggregationResponderThread has ended");
    }

    //Responds depending on GET or PUT request
    private void respond(HTTPRequestReader reader, HTTPResponseWriter writer) {
        String lamportClockString = reader.getHeader("lamport-clock");
        //If there's no lamport clock value, send a bad request message
        if (lamportClockString == null) {
            writer.writeResponse(400, "You must include a lamport-clock header inside your request", lamportClock.incrementAndGet());
            System.out.println("Received message without lamport-clock value");
            return;
        }

        //If the lamport clock value has a non-numerical character
        if (!lamportClockString.matches("\\d*")) {
            writer.writeResponse(400, "the lamport-clock value must be a valid long value", lamportClock.incrementAndGet());
            System.out.println("Received invalid lamport-clock value");
            return; 
        }

        lamportClock.setMaxAndIncrement(Long.parseLong(lamportClockString));

        //Return bad response if unimplemented methods
        if (!reader.getMethod().equals("GET") && !reader.getMethod().equals("PUT")) {
            writer.writeResponse(400, "Only GET and PUT is implemented on this server", lamportClock.incrementAndGet());
            System.out.println("Received unimplmented method.");
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
            writer.writeResponse(404, "GET resource does not exist. Only resource available is '/'", lamportClock.incrementAndGet());
            return;
        }

        //TODO: Create retrieval method
        writer.writeResponse(200, "looks okay.", lamportClock.incrementAndGet());
    }

    //Sends a response for a PUT request
    private void handlePUT(HTTPRequestReader reader, HTTPResponseWriter writer) {
        if (reader.getURL().equals("/atom.xml")) {
            writer.writeResponse(404, "PUT resource does not exist. Only resource available is '/atom.xml'", lamportClock.incrementAndGet());
            return;
        }

        if (reader.getHeader("Content-Length") == null) {
            writer.writeResponse(411, "Content-Length header missing.", lamportClock.incrementAndGet());
            return;
        }

        if (reader.getHeader("Content-Length").equals("0")) {
            writer.writeResponse(204, "Body is empty. Did nothing", lamportClock.incrementAndGet());
            return;
        }

        //TODO: Create saving method
        writer.writeResponse(200, "looks okay.", lamportClock.incrementAndGet());
    }
}
