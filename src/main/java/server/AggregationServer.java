package main.java.server;


import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class AggregationServer { 
    public static void main(String[] args) {

        //Parse hostname and port number
        int portNumber = 4567;
        if (args.length == 1) {
            portNumber = Integer.parseInt(args[0]);
        } else if (args.length > 1) {
            System.err.println("Usage: java AggregationServer <port number>");
            System.err.println("If <port number> is empty, the default is 4567");
            System.exit(1);
        }

        //Start accepting connections
        long connectionId = 0;
        System.out.println("Starting server on port: " + portNumber);
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            //Starts an AggregationResponderThread
            while (true) {
                new AggregationResponderThread(serverSocket.accept(), connectionId++).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }
}
