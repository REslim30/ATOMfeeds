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
        System.out.println("Listener Thread starting");
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

        System.out.println("Listener Thread got:\n" + request.toString()); 

        // try {
        //     requests.put(socket);
        // } catch (InterruptedException e) {
        //     System.err.println("Error in ListenerThread: " + e.toString());
        //     e.printStackTrace();
        // }
        System.out.println("Listener Thread Ending");
    }
}
