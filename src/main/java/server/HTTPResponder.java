package main.java.server;

import java.net.*;
import java.io.*;


public class HTTPResponder extends Thread {
    private Socket socket = null;

    public HTTPResponder(Socket socket) {
        this.socket = socket;
    }
    
    public void run() {

        try (
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream()));
        ) {
            while (true) {
                String inputLine;
                while((inputLine = in.readLine()) != null) {
                    System.out.println("Thread received: " + inputLine);
                    if (inputLine.isEmpty())
                        break;
                }

                
                System.out.println("Thread now sending to client");
                out.print("HTTP/1.1 200 OK\r\n");
                out.print("Content-Type: text/plain\r\n");
                out.print("Content-Length: 30\r\n");
                out.print("\r\n");
                out.print("Thread says bye :(");
                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }     
    }
}

