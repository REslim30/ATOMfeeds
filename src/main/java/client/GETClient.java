package main.java.client;

import java.io.*;
import java.net.*;
import java.util.*;


public class GETClient {
    public static void main(String[] args) {
                
        //Parse hostname and port number
        if (args.length != 2) {
            System.err.println(
                "Usage: java GETClient <host name> <port number>");
            System.exit(1);
        }
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);


        //Connect to host
        System.out.println("Connecting to:" + hostName + ':' + portNumber);
        try (
            Socket socket = new Socket(hostName, portNumber);
            PrintWriter out =
                new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
        ) {
            enterConnection(in, out, hostName);
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } 
    }

    //Enters a persistent connection with the host
    //Allows user various actions
    private static void enterConnection(BufferedReader in, PrintWriter out, String hostName) {
            try (Scanner stdIn = new Scanner(System.in)) {
                while (true) {
                    System.out.println("\nPlease enter:");
                    System.out.println("0    - to close the connection");
                    System.out.println("1    - to send a GET request");
                    switch(stdIn.nextLine()) {
                        case "0":
                            System.out.println("closing the connection");
                            return;
                        case "1":
                            System.out.println("Sending Get request");
                            sendRequest(out, hostName);

                            System.out.println("Server sent back:");
                            if (!receiveResponse(in)) {
                                System.out.println("Server wants to close connection");
                                System.out.println("closing connection");
                                return;
                            }
                            break;
                        default:
                            System.out.println("Invalid Input");
                            break;
                    }
                }
            }
    }

    //Sends a basic HTTP request
    private static void sendRequest(PrintWriter out, String hostName) {
        out.print("GET / HTTP/1.1\r\n");
        out.print("Host: " + hostName + "\r\n");
        out.print("Connection: keep-alive\r\n");
        out.print("Cache-Control: no-cache\r\n");
        out.print("\r\n");
        out.flush();
    }

    //Receives the response
    //And prints to stdin
    //Returns false if server 
    //wants to close connection
    private static boolean receiveResponse(BufferedReader in) {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
        } catch (IOException e) {
            System.err.println("Error in receiving response.:");
            System.err.println(e.toString());
        }
        return true;
    }
}