package client;


import java.io.*;
import java.net.*;
import java.util.*;
import org.xml.sax.SAXException;

import atom.InvalidAtomException;
import atom.AtomParser;
import http.*;



public class GETClient {
    private static long lamportClock = 0;
    private static String resource = null;
    public static void main(String[] args) {
        
        //Parse hostname and port number and resource
        if (args.length != 1) {
            System.err.println(
                "Usage: java GETClient <host name>:<port number><resource>");
            System.exit(1);
        }

        URL url = URLParser.parseURL(args[0]);
        String hostName = url.getHost();
        int portNumber = url.getPort();
        resource = url.getPath();
        if (portNumber == -1) {
            System.out.println("Port Number unspecified. Default is 4567.");
            portNumber = 4567;
        }
        if (resource.isBlank()) {
            System.out.println("Resource unspecified. Resource set to '/'");
            resource = "/";
        }



        //Connect to host
        System.out.println("Connecting to " + hostName + ':' + portNumber);
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
            out.print("GET " + resource + " HTTP/1.1\r\n");
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

    //Reads in aggregated atom documents.
    //Prints to std out
    //Throws an InvalidAtomException on invalid Atom
    //Throws an SAXException on invalid XML
    //Throws an IOException on any IO errors
    //Assumes aggregated atom documents are separated by newline characters
    private static void printResponse(String body) {
         try (Scanner scanner = new Scanner(body)) {
             while (scanner.hasNext()) {
                 StringBuilder atomDocument = new StringBuilder();
                 while (scanner.hasNextLine()) {
                     String line = scanner.nextLine();
                     atomDocument.append(line);
                     atomDocument.append("\n");
                     if (line.matches("</feed>$"))
                         break;
                 }

                 //Parses Atom
                 try {
                     AtomParser parser = new AtomParser(atomDocument.toString());
                     parser.parseAtom();
                     System.out.println(parser.getPrettyFeed());
                 } catch (IOException ioe) {
                     System.err.println(ioe.getMessage());
                     ioe.printStackTrace();
                 } catch (SAXException saxe) {
                     System.err.println("Invalid XML: " + saxe.getMessage());
                 } catch (InvalidAtomException atome) {
                     System.err.println("Invalid Atom: " + atome.getMessage());
                 }
             }
         }

    }

    //Logs a response with lamportClock
    private static void log(String input) {
        System.out.println("Lamport Clock = " + Long.toString(lamportClock) + "    ->   " + input);
    }


}
