package client;


import java.io.*;
import java.net.*;
import java.util.*;
import http.*;



public class GETClient {
    private static long lamportClock = 0;
    public static void main(String[] args) {
        
        //Parse hostname and port number
        if (args.length != 1) {
            System.err.println(
                "Usage: java GETClient <host name>:<port number>");
            System.exit(1);
        }

        //TODO: Show user error when hostName/PortNumber is invalild
        URL url = URLParser.parseURL(args[0]);
        String hostName = url.getHost();
        int portNumber = url.getPort();

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
                        log("closing the connection");
                        return;
                    case "1":
                        //If server has closed the connection
                        //Exit function
                        if (out.checkError()) 
                            return;
                        
                        log("Sending Get request");

                        lamportClock++;
                        sendRequest(out, hostName);

                        log("Server sent back:");
                        if (!receiveResponse(in)) {
                            log("Server wants to close connection");
                            log("closing connection");
                            return;
                        }
                        break;
                    default:
                        log("Invalid Input");
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error while managing connection   -   " + e.getMessage());
        }
    }

    //Sends a basic HTTP request
    private static void sendRequest(PrintWriter out, String hostName) throws IOException {
            out.print("GET / HTTP/1.1\r\n");
            out.print("Host: " + hostName + "\r\n");
            out.print("User-Agent: ATOMGETClient/1/0\r\n");
            out.print("Connection: keep-alive\r\n");
            out.print("Lamport-Clock: " + Long.toString(lamportClock) + "\r\n");
            out.print("\r\n");
            out.flush();

            if (out.checkError())
                throw new IOException("Seem to have lost connection");
    }

    //Receives the response
    //And prints to stdin
    //Returns false if server wants to close connection
    //Also updates lamportClock value
    private static boolean receiveResponse(BufferedReader in) throws IOException {
        HTTPResponseReader response = new HTTPResponseReader(in);
        response.readResponse();
        printResponse(response.getBody());

        String lamportClockString = response.getHeader("lamport-clock");
        if (lamportClockString != null && lamportClockString.matches("\\d*")) {
            lamportClock = Long.max(lamportClock, Long.parseLong(lamportClockString));
            lamportClock++;
        }

        String connection = response.getHeader("Connection");
        return (connection == null || connection.equals("keep-alive"));
    }

    //TODO:
    private static void printResponse(String body) {
        
    }

    //Logs a response with lamportClock
    private static void log(String input) {
        System.out.println("Lamport Clock = " + Long.toString(lamportClock) + "    ->   " + input);
    }


}
