package main.java.content;

import java.io.*;
import java.net.*;
import java.util.*;
import main.java.http.*;

public class ContentServer {
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

    }
}