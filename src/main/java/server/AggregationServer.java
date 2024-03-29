package server;


import java.io.*;
import java.net.*;
import java.sql.SQLException;

/**
 * AggregationServer
 * 
 * The main thread of the AggregationServer.
 * Starts AggregationDeleterThread
 * Listens and Binds new connections to new AggregationResponderThreads.
 */
public class AggregationServer { 
    private static LamportClock lamportClock = new LamportClock(0);
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
        System.out.println("Port unspecified, port is now set to 4567");

        //Create a storage manager for threads to share
        AggregationStorageManager storage = new AggregationStorageManager();

        //Create a thread that deletes 12 second old feed every second
        new AggregationDeleterThread(storage).start();

        //Start accepting connections
        System.out.println("Starting server on port: " + portNumber);
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            //Starts an AggregationResponderThread
            while (true) {
                new AggregationResponderThread(serverSocket.accept(), lamportClock, storage).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }

    }
}
