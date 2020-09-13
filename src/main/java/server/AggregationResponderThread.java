package main.java.server;

import java.net.*;
import java.io.*;
import java.sql.SQLException;
import main.java.http.HTTPRequestReader;
import main.java.http.HTTPResponseWriter;

/**
 * AggregationResponderThread
 * Listens and responds to a single client
 */
public class AggregationResponderThread extends Thread {
    //needed to handle lamport clocks that are of equal value
    private LamportClock lamportClock;
    private Socket socket;
    private AggregationStorageManager storage;

    public AggregationResponderThread(Socket socket, LamportClock lamportClock, AggregationStorageManager storage) {
        this.socket = socket;
        this.lamportClock = lamportClock;
        this.storage = storage;
    }
    
    public void run() {
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

                System.out.println(socket.getRemoteSocketAddress().toString());

                respond(reader, writer);
                //end connection if client wants to end connection
                if (reader.getHeader("connection") != null && reader.getHeader("connection").equals("close"))
                    break;
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("AggregationResponderThread: Error while managing connection: " + e.toString());
        }

        
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

        //Return bad response if unimplemented methods
        if (!reader.getMethod().equals("GET") && !reader.getMethod().equals("PUT")) {
            writer.writeResponse(400, "Only GET and PUT is implemented on this server", lamportClock.incrementAndGet());
            System.out.println("Received unimplmented method.");
            return;
        }

        synchronized(lamportClock) {
            lamportClock.setMaxAndIncrement(Long.parseLong(lamportClockString));

            //Handle GET and PUT requests
            if (reader.getMethod().equals("GET")) {
                handleGET(reader, writer);
            } else {
                handlePUT(reader, writer);
            }     
        }

    }
    

    //Sends a response for a GET request
    private void handleGET(HTTPRequestReader reader, HTTPResponseWriter writer) {
        if (reader.getURL().equals("/")) {
            writer.writeResponse(404, "GET resource does not exist. Only resource available is '/'", lamportClock.incrementAndGet());
            return;
        }

        //Sends all feeds to client
        try {
            String body = storage.retrieveAllFeeds();
            writer.writeResponse(200, body, lamportClock.incrementAndGet());
            System.out.println("Retrieved feeds for GET client");
        } catch (SQLException e) {
            writer.writeResponse(500, "Server couldn't retrieve the feeds: " + e.toString(), lamportClock.incrementAndGet());
            System.out.println("Couldn't retrieve feeds for GET client: " + e.toString());
            e.printStackTrace();
        }
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

        //Save a feed
        try {
            if (storage.saveFeed(socket.getRemoteSocketAddress().toString(), reader.getBody())) {
                writer.writeResponse(201, "Created new feed", lamportClock.incrementAndGet());
                System.out.println("Created new feed");
            } else {
                writer.writeResponse(200, "Updated feed", lamportClock.incrementAndGet());
                System.out.println("Updated feed");
            }
        } catch (SQLException e) {
            writer.writeResponse(500, "Server Couldn't save your feed" + e.toString(), lamportClock.incrementAndGet());
            System.out.println("Error while trying to save a feed: " + e.toString());
            e.printStackTrace();
        }
    }
}
