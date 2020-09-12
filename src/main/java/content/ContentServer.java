package main.java.content;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.System;
import main.java.http.*;

public class ContentServer {
    public static void main(String[] args) {
        //Parse hostname and port number
        if (args.length != 2) {
            System.err.println(
                "Usage: java ContentServer <host name>:<port number> <file name>");
            System.exit(1);
        }

        //TODO: Show user error when hostName/PortNumber is invalild
        URL url = URLParser.parseURL(args[0]);
        String hostName = url.getHost();
        int portNumber = url.getPort();
        String fileName = args[1];

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
            enterConnection(in, out, hostName, fileName);
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
    private static void enterConnection(BufferedReader in, PrintWriter out, String hostName, String fileName) {
        try (Scanner stdIn = new Scanner(System.in)) {
            while (true) {
                System.out.println("\nPlease enter:");
                System.out.println("0    - to close the connection");
                System.out.println("1    - to send a PUT request");
                switch(stdIn.nextLine()) {
                    case "0":
                        System.out.println("closing the connection");
                        return;
                    case "1":
                        //If server has closed the connection
                        //Exit function
                        if (out.checkError()) 
                            return;
                        
                        System.out.println("Sending PUT request");
                        sendRequest(out, hostName, fileName);

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
    private static void sendRequest(PrintWriter out, String hostName, String fileName) {
        //First read in a file
        try (BufferedReader file = new BufferedReader(
                                        new InputStreamReader(
                                            ClassLoader.getSystemClassLoader()
                                                .getResourceAsStream(fileName)))) {
            String line;
            StringBuilder bodyBuilder = new StringBuilder();
            while ((line = file.readLine()) != null)  {
               bodyBuilder.append(line); 
               bodyBuilder.append('\n');
            }
            String body = bodyBuilder.toString();
            
            //Send the request
            out.print("PUT /atom.xml HTTP/1.1\r\n");
            out.print("Host: " + hostName + "\r\n");
            out.print("User-Agent: ATOMClient/1/0\r\n");
            out.print("Connection: keep-alive\r\n");
            out.print("Content-Type: application/atom+xml" + "\r\n");
            out.print("Content-Length: " + Integer.toString(body.length()) + "\r\n");
            out.print("\r\n");
            out.print(body);
            out.flush();
            
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't find the file you suggested: " + fileName + "\n" + e.toString());
            System.exit(1);
        } catch (IOException ioe) {
            System.err.println("Error in reading file: " + ioe.toString());
        }
    }

    //Receive the response
    //Print relevant information to stdin
    //Returns false if server wants to end the connection
    private static boolean receiveResponse(BufferedReader in) {
        HTTPResponseReader response = new HTTPResponseReader(in);
        response.readResponse();
        System.out.println("Server responds with: " + response.getStatusCode() + " " + response.getStatusMsg());
        System.out.println("Body: " + response.getBody());

        String connection = response.getHeader("Connection");
        return (connection == null || connection.equals("keep-alive"));
    }
}
