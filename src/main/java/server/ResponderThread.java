package main.java.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class ResponderThread extends Thread {
    private BlockingQueue<Socket> requests;
    
    //True contructor
    public ResponderThread(BlockingQueue<Socket> requests) {
        this.requests = requests;
    }

    public void run() {
        while (true) {
            //Grab next connection from the queue
            Socket socket = null;
            try {
                socket = requests.take();
            } catch (InterruptedException e) {
                System.err.println("Error while waiting for socket ResponderThread: " + e.toString());
            }

            //Respond to the request
            try (
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                        socket.getInputStream()));
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                }
                socket.close();
            } catch (IOException e) {
                System.err.println("Error in ResponderThread: " + e.toString());
            }
        }
    }
}
