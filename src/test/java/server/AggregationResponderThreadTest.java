package server;

import server.AggregationResponderThread;
import http.*;

import static org.junit.Assert.assertEquals;
import org.junit.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AggregationResponderThreadTest
 *
 * Tests to ensure 1 to 1 connections are being handled correctly
 * Mainly tests HTTP responses in responses to certain messages
 * Validitiy of HTTP responses are tested in HTTPResponseWriter
 */
public class AggregationResponderThreadTest {
    private int portNumber = 3000;
    private LamportClock lamportClock = new LamportClock(0);
    AggregationStorageManager storage = new AggregationStorageManager();
    AggregationResponderThread responderThread = null;

    @Before
    public void initialize() throws IOException {
        //Responder Thread uses standard err to log
        //And standard 
        //We don't need that here
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
    }

    //Sends a 400 response if bad request
    @Test
    public void Response400IfInvalidHTTP() throws IOException{ 
        Socket socket = initiateConnection();
        PrintWriter out = getOutput(socket);
        BufferedReader in = getInput(socket);

        out.print("Invalid HTTP request\r\b");
        out.flush();

        assertEquals("HTTP/1.1 400 Bad Request", in.readLine());
        
        socket.close();
    }

    // Sends a 400 response if no lamport clock value
    @Test
    public void response400IfNoLamport() throws IOException {
        Socket socket = initiateConnection();
        PrintWriter out = getOutput(socket);
        BufferedReader in = getInput(socket);

        out.print("GET / HTTP/1.1\r\n");
        out.print("\r\n");
        out.flush();

        assertEquals("HTTP/1.1 400 Bad Request", in.readLine());

        socket.close();
    }

    // Sends a 400 response if lamport clock invalid
    @Test
    public void response400IfLamportInvalid() throws IOException {
        Socket socket = initiateConnection();
        PrintWriter out = getOutput(socket);
        BufferedReader in = getInput(socket);

        out.print("GET / HTTP/1.1\r\n");
        out.print("Lamport-Clock: abc\r\n");
        out.print("\r\n");
        out.flush();

        assertEquals("HTTP/1.1 400 Bad Request", in.readLine());

        socket.close();
    }

    // Sends a 400 response if unimplmented method
    @Test
    public void response400IfUnimplementedMethod() throws IOException {
        Socket socket = initiateConnection();
        PrintWriter out = getOutput(socket);
        BufferedReader in = getInput(socket);

        out.print("POST / HTTP/1.1\r\n");
        out.print("Lamport-Clock: 1234\r\n");
        out.print("\r\n");
        out.flush();

        assertEquals("HTTP/1.1 400 Bad Request", in.readLine());

        socket.close();
    }


    // Sends a 200 response if request is valid
    @Test
    public void response200IfValidGet() throws IOException {
        Socket socket = initiateConnection();
        PrintWriter out = getOutput(socket);
        BufferedReader in = getInput(socket);

        out.print("GET / HTTP/1.1\r\n");
        out.print("Lamport-Clock: 1234\r\n");
        out.print("\r\n");
        out.flush();

        assertEquals("HTTP/1.1 200 OK", in.readLine());

        socket.close();
    }


    @Test
    public void response404IfGetInvalidResource() throws IOException {
        Socket socket = initiateConnection();
        PrintWriter out = getOutput(socket);
        BufferedReader in = getInput(socket);

        out.print("GET /resource HTTP/1.1\r\n");
        out.print("Lamport-Clock: 1234\r\n");
        out.print("\r\n");
        out.flush();

        assertEquals("HTTP/1.1 404 Not Found", in.readLine());
        
        socket.close();
    }


    @Test
    public void response404IfPutInvalidResource() throws IOException {
        Socket socket = initiateConnection();
        PrintWriter out = getOutput(socket);
        BufferedReader in = getInput(socket);

        out.print("PUT /resource HTTP/1.1\r\n");
        out.print("Lamport-Clock: 1234\r\n");
        out.print("\r\n");
        out.flush();

        assertEquals("HTTP/1.1 404 Not Found", in.readLine());
        
        socket.close();
    }

    @Test
    public void response411IfPutWithoutContentLength() throws IOException {
        Socket socket = initiateConnection();
        PrintWriter out = getOutput(socket);
        BufferedReader in = getInput(socket);

        out.print("PUT /atom.xml HTTP/1.1\r\n");
        out.print("Lamport-Clock: 1234\r\n");
        out.print("\r\n");
        out.flush();

        assertEquals("HTTP/1.1 411 Length Required", in.readLine());
        
        socket.close();
    }

    @Test
    public void response204IfPutWithContentLengthZero() throws IOException {
        Socket socket = initiateConnection();
        PrintWriter out = getOutput(socket);
        BufferedReader in = getInput(socket);

        out.print("PUT /atom.xml HTTP/1.1\r\n");
        out.print("Lamport-Clock: 1234\r\n");
        out.print("Content-Length: 0\r\n");
        out.print("\r\n");
        out.flush();

        assertEquals("HTTP/1.1 204 No Content", in.readLine());
        
        socket.close();
    }

    @Test
    public void response400IfPutWithInvalidXML() throws IOException {
        Socket socket = initiateConnection();
        PrintWriter out = getOutput(socket);
        BufferedReader in = getInput(socket);

        out.print("PUT /atom.xml HTTP/1.1\r\n");
        out.print("Lamport-Clock: 1234\r\n");
        out.print("Content-Length: 14\r\n");
        out.print("\r\n");
        out.print("Invalid<><>XML");
        out.flush();

        assertEquals("HTTP/1.1 400 Bad Request", in.readLine());
        
        socket.close();
    }

    @Test
    public void response400IfPutWithInvalidAtom() throws IOException {
        Socket socket = initiateConnection();
        PrintWriter out = getOutput(socket);
        BufferedReader in = getInput(socket);

        out.print("PUT /atom.xml HTTP/1.1\r\n");
        out.print("Lamport-Clock: 1234\r\n");
        out.print("Content-Length: 13\r\n");
        out.print("\r\n");
        out.print("<feed></feed>");
        out.flush();

        assertEquals("HTTP/1.1 400 Bad Request", in.readLine());
        
        socket.close();
    }

    //General integration test for the basic workflow
    @Test
    public void response201FirstThen200WhenValidPuts() throws IOException {
        Socket socket = initiateConnection();
        PrintWriter out = getOutput(socket);
        BufferedReader in = getInput(socket);

        String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<feed xml:lang=\"en-US\" xmlns=\"https://www.w3.org/2005/Atom\">\n  <title>My example feed</title>\n  <subtitle>for demonstration purposes</subtitle>\n  <link href=\"www.cs.adelaide.edu.au\"></link>\n  <updated>2015-08-07T18:30:02Z</updated>\n  <author>\n    <name>Santa Claus</name>\n  </author>\n  <id>urn::uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>\n  <entry>\n    <title>Nick sets assignment</title>\n    <link href=\"www.cs.adelaide.edu.au/users/third/ds/\"></link>\n    <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>\n    <updated>2015-08-07T18:30:02Z</updated>\n    <summary>here is some plain text. Because I'm not completely evil, you can assume that this will always be less than 1000 characters. And, as I've said before, it will always be plain text.</summary>\n  </entry>\n  <entry>\n    <title>second feed entry</title>\n    <link href=\"www.cs.adelaide.edu.au/users/third/ds/14ds2s1\"></link>\n    <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6b</id>\n    <updated>2015-08-07T18:29:02Z</updated>\n    <summary>here's another summary entry which a reader would normally use to work out if they wanted to read some more. It's quite handy.</summary>\n  </entry>\n</feed>\n";

        out.print("PUT /atom.xml HTTP/1.1\r\n");
        out.print("Lamport-Clock: 1234\r\n");
        out.print("Content-Length: " + body.length() + "\r\n");
        out.print("\r\n");
        out.print(body);
        out.flush();

        HTTPResponseReader reader = new HTTPResponseReader(in);
        reader.readResponse();
        
        assertEquals(201, reader.getStatusCode());

        out.print("PUT /atom.xml HTTP/1.1\r\n");
        out.print("Lamport-Clock: 1234\r\n");
        out.print("Content-Length: " + body.length() + "\r\n");
        out.print("\r\n");
        out.print(body);
        out.flush();

        reader = new HTTPResponseReader(in);
        reader.readResponse();
        
        assertEquals(200, reader.getStatusCode());


        out.print("GET / HTTP/1.1\r\n");
        out.print("Lamport-Clock: 1234\r\n");
        out.print("\r\n");
        out.flush();

        reader = new HTTPResponseReader(in);
        reader.readResponse();

        assertEquals(200, reader.getStatusCode());
        assertEquals(body, reader.getBody());

        socket.close();
    }

    //Closes thread and gives time for socket to be free.
    @After
    public void closeThread() throws InterruptedException {
        responderThread.join();
    }

    private PrintWriter getOutput(Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream(), true);
    }

    private BufferedReader getInput(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    //Mocks a server and initiates a Socket connection with a mock thread
    private Socket initiateConnection() throws IOException {
        //Start a Thread that acts as a server
        //Accepts one connection and then terminates
        Thread serverThread = new Thread() {
            @Override
            public void run() {
                try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
                     responderThread = new AggregationResponderThread(serverSocket.accept(), lamportClock, storage);
                     responderThread.start();
                } catch (IOException ioe) {
                    System.err.println(ioe.getMessage());
                    ioe.printStackTrace();
                }
            }
        };

        serverThread.start();

        //Continues to try and connect to server
        Socket socket = null; 
        while (true) {
            try {
                socket = new Socket("localhost", portNumber);
                break;
            } catch (ConnectException e) {
                try {
                    Thread.sleep(1000);
                } catch (Exception io) {
                    System.err.println(io.getMessage());
                    io.printStackTrace();
                }
            }
        }
        return socket;
    }
}
