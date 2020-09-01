package main.java.client;

import java.io.*;
import java.net.*;


public class GETClient {
    public static void main(String[] args) {
                
        if (args.length != 2) {
            System.err.println(
                "Usage: java GETClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (
            Socket echoSocket = new Socket(hostName, portNumber);
            PrintWriter out =
                new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
            BufferedReader stdIn =
                new BufferedReader(
                    new InputStreamReader(System.in))
        ) {
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.print(userInput + "\r\n");
                if (userInput.isEmpty())
                    break;
            }
            out.flush();

            System.out.println("Now reading");
            while ((userInput = in.readLine()) != null) {
                System.out.println("client: " + userInput);
            }
            echoSocket.close();
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } 
    }
}