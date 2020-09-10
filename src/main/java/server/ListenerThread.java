package main.java.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import main.java.http.*;

public class ListenerThread extends Thread {
    private Socket socket = null;
    private BlockingQueue<Socket> requests;

    public ListenerThread(BlockingQueue<Socket> requests, Socket socket) {
        this.socket = socket;
        this.requests = requests;
    }

    public void run() {
        System.out.println("ListenerThread has started");
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
            System.err.println("ListenerThread: Error while reading in request: " + e.toString());
        }

        //Stop if there are is no PID or LamportClock value in request
        //TODO: Send a 400 - Bad request
        if (request.getHeader("PID") == null || request.getHeader("Lamport-Clock") == null || request.getHeader("IP") == null) {
            System.err.println("ListenerThread: Error in request. Server requires PID, Lamport-Clock, and IP header values");
            System.exit(1);
        }


        System.out.println(request.getHeader("IP-PID"));
        System.out.println(request.getHeader("Lamport-Clock"));

        //Puts the request onto the queue
        // try {
        //     requests.put(socket);
        // } catch (InterruptedException e) {
        //     System.err.println("Error in ListenerThread: " + e.toString());
        //     e.printStackTrace();
        // }
    }
}
