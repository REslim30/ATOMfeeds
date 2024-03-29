package server;

import java.net.*;
import java.io.*;
import atom.*;
import org.xml.sax.SAXException;
import java.sql.SQLException;
import http.HTTPRequestReader;
import http.HTTPResponseWriter;

/**
 * AggregationResponderThread
 * 
 * Listens and Responds to a single connection.
 * This is where all of the policies for handling requests
 * reside.
 */
public class AggregationResponderThread extends Thread {
    //needed to handle lamport clocks that are of equal value
    private LamportClock lamportClock;
    private Socket socket;
    private AggregationStorageManager storage;

    public AggregationResponderThread(Socket socket, LamportClock lamportClock, AggregationStorageManager storage) {
        //
        try {
            socket.getKeepAlive();
            socket.setSoTimeout(15000);
        } catch (SocketException soe) {
            System.err.println("Couldn't set Keep-Alive or Timeouts on");
            System.err.println(soe.getMessage());
            soe.printStackTrace();
        }

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
                try {
                    reader = new HTTPRequestReader(in);
                    writer = new HTTPResponseWriter(out);
                    reader.readRequest();

                    System.out.println("Message from:" + socket.getRemoteSocketAddress().toString());

                    respond(reader, writer);
                    //end connection if client wants to end connection
                    if (reader.getHeader("connection") != null && reader.getHeader("connection").equals("close"))
                        break;

                } catch (SocketTimeoutException se) {
                    //If we've been blocked for 15 seconds, close connection
                    System.err.println("Connection: " + socket.getRemoteSocketAddress().toString() + "  -  15 seconds have elapsed and no request");
                    System.err.println("Closing Connection");
                    break;
                } catch (IOException ioe) {
                    //Possible HTTP syntax/ atom protocol error
                    //Or client decides to close connection
                    System.err.println("Connection: " + socket.getRemoteSocketAddress().toString() + "  -  " + ioe.getMessage());
                    writer.writeResponse(400, "I/O Error - Please ensure your request is a valid HTTP request: " + ioe.getMessage(), lamportClock.incrementAndGet());
                    break;
                }             
            }

            socket.close();
        } catch (IOException e) {
            //IOException for getting the connection
            System.err.println("Connection: " + socket.getRemoteSocketAddress().toString() + "  -  " + e.getMessage());
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

        //Handle GET and PUT requests
        if (reader.getMethod().equals("GET")) {
            handleGET(reader, writer);
        } else {
            handlePUT(reader, writer);
        }     
    }
    

    //Sends a response for a GET request
    private void handleGET(HTTPRequestReader reader, HTTPResponseWriter writer) {
        if (!reader.getURL().equals("/")) {
            writer.writeResponse(404, "GET resource does not exist. Only resource available is '/'", lamportClock.incrementAndGet());
            return;
        }

        synchronized(lamportClock) {
            String lamportClockString = reader.getHeader("lamport-clock");
            lamportClock.setMaxAndIncrement(Long.parseLong(lamportClockString));
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
    }

    //Sends a response for a PUT request
    private void handlePUT(HTTPRequestReader reader, HTTPResponseWriter writer) {
        if (!reader.getURL().equals("/atom.xml")) {
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

        //Ensure Atom content is valid
        try {
            AtomParser parser = new AtomParser(reader.getBody());            
            parser.parseAtom();
        } catch (InvalidAtomException ae) {
            System.err.println(ae.getMessage());
            writer.writeResponse(400,"Invalid Atom: " + ae.getMessage(), lamportClock.incrementAndGet()); 
            return;
        } catch (SAXException se) {
            System.err.println(se.getMessage());
            writer.writeResponse(400, "Invalid XML: " + se.getMessage() , lamportClock.incrementAndGet());
            return;
        } catch (IOException ie) {
            System.err.println(ie.getMessage());
            writer.writeResponse(500, "I/O Error on server: " + ie.getMessage() , lamportClock.incrementAndGet());
            return;
        }

        synchronized(lamportClock) {
            String lamportClockString = reader.getHeader("lamport-clock");
            lamportClock.setMaxAndIncrement(Long.parseLong(lamportClockString));

            //Save a feed
            try {
                System.out.println(reader.getBody());
                if (storage.saveFeed(socket.getRemoteSocketAddress().toString(), reader.getBody())) {
                    writer.writeResponse(201, "Created new feed", lamportClock.incrementAndGet());
                    System.out.println("Created new feed");
                    return;
                } else {
                    writer.writeResponse(200, "Updated feed", lamportClock.incrementAndGet());
                    System.out.println("Updated feed");
                    return;
                }
            } catch (SQLException e) {
                writer.writeResponse(500, "Server Couldn't save your feed" + e.toString(), lamportClock.incrementAndGet());
                System.out.println("Error while trying to save a feed: " + e.toString());
                e.printStackTrace();
                return;
            }
        }
    }
}
