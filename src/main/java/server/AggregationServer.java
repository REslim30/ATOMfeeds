package main.java.server;

import java.net.*;
import java.io.*;

public class AggregationServer {
    public static void main(String[] args) throws IOException {

    int portNumber;
    if (args.length == 1) {
        portNumber = Integer.parseInt(args[0]);
    } else {
        System.out.println("Running automatically on port 3000");
        portNumber = 3000;
    }

        
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (true) {
                System.out.println("Aggregation Server starting new thread:");
	            new HTTPResponder(serverSocket.accept()).start();
	        }
	    } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }

}