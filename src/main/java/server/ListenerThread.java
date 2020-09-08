package main.java.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class ListenerThread extends Thread {
    private Socket socket = null;
    private BlockingQueue<Socket> requests;

    public ListenerThread(BlockingQueue<Socket> requests, Socket socket) {
        this.socket = socket;
        this.requests = requests;
    }

    public void run() {
        System.out.println("Listener Thread starting");
        try {
            requests.put(socket);
        } catch (InterruptedException e) {
            System.err.println("Error in ListenerThread: " + e.toString());
            e.printStackTrace();
        }
        System.out.println("Listener Thread Ending");
    }
}
