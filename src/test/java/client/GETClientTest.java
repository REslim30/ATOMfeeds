package client;

import client.GETClient;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.After;

import java.io.*;
import java.util.regex.*;

/**
 * GETClientTest
 */
public class GETClientTest extends GETClient {

    //Testing Response Printing
    @Test
    public void logsErrorIfInvalidXML() {
        ByteArrayOutputStream err = setNewErr();
        printResponse("invalid XML since no tags .><?:");
        assertEquals(true, caseInsensitiveMatch(err.toString(), "invalid xml"));
    }

    @Test
    public void logsErrorIfInvalidAtom() {
        ByteArrayOutputStream err = setNewErr();
        printResponse("<feed></feed>");
        assertEquals(true, caseInsensitiveMatch(err.toString(), "invalid atom"));
    }

    @Test
    public void logsErrorIfCloseAtom() {
        ByteArrayOutputStream err = setNewErr();
        printResponse("<feed>" +
            "<title>test title</title>" + 
            "<id>123456</id>" + 
            "<update>123456</update>" + 
            "</feed>");
        assertEquals(true, caseInsensitiveMatch(err.toString(), "invalid atom"));
    }

    @Test
    //Basic integration testing only. Comprehensive testing already done via 
    //AtomParserTest
    public void printsIfCorrectAtom() {
        ByteArrayOutputStream out = setNewOut();
        printResponse("<feed>" +
            "<title>test title</title>" + 
            "<id>123456</id>" + 
            "<updated>123456</updated>" + 
            "</feed>");
        assertEquals(true, caseInsensitiveMatch(out.toString(), "title: test title"));
        assertEquals(true, caseInsensitiveMatch(out.toString(), "id: 123456"));
        assertEquals(true, caseInsensitiveMatch(out.toString(), "updated: 123456"));
    }

    //Parses multiple lines
    @Test
    public void printsMultipleFeeds() {
        ByteArrayOutputStream out = setNewOut();
        printResponse(
            "<feed>" +
            "<title>test title</title>" + 
            "<id>123456</id>" + 
            "<updated>123456</updated>" + 
            "</feed>\n" +
            "<feed>" +
            "<title>second title</title>" + 
            "<id>idno2</id>" + 
            "<updated>54321</updated>" + 
            "</feed>"
            );
        assertEquals(true, caseInsensitiveMatch(out.toString(), "title: test title"));
        assertEquals(true, caseInsensitiveMatch(out.toString(), "id: 123456"));
        assertEquals(true, caseInsensitiveMatch(out.toString(), "updated: 123456"));

        assertEquals(true, caseInsensitiveMatch(out.toString(), "title: second title"));
        assertEquals(true, caseInsensitiveMatch(out.toString(), "id: idno2"));
        assertEquals(true, caseInsensitiveMatch(out.toString(), "updated: 54321"));
    }

    
    @Test(expected = IOException.class)
    public void throwsIOExceptionOnInvalidHTTP() throws IOException {
        receiveResponse(parseResponse("Invalid HTTP Since Status Line Too Long"));
    }

    @Test
    public void updatesLamport() throws IOException {
        receiveResponse(parseResponse(
            "HTTP/1.1 200 OK\r\n" + 
            "Lamport-Clock: 200\r\n" +
            "\r\n"
        ));
        assertEquals(201, lamportClock);
    }

    @Test
    public void returnsTrueIfConnectionIsContinue() throws IOException {
        boolean result = receiveResponse(parseResponse(
            "HTTP/1.1 200 OK\r\n" + 
            "Connection: keep-alive\r\n" +
            "\r\n"
        ));

        assertEquals(true, result);
    }

    @Test
    public void returnsFalseIfConnectionIsNotContinue() throws IOException {
        boolean result = receiveResponse(parseResponse(
            "HTTP/1.1 200 OK\r\n" + 
            "Connection: close\r\n" +
            "\r\n"
        ));

        assertEquals(false, result);
    }

    @Test
    public void sendsBasicResponse() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        resource = "/";
        lamportClock = 200;
        sendRequest(new PrintWriter(out), "hostname");

        assertEquals(out.toString(), 
            "GET / HTTP/1.1\r\n" +
            "Host: hostname\r\n" +
            "User-Agent: ATOMGETClient/1/0\r\n" +
            "Connection: keep-alive\r\n" +
            "Lamport-Clock: 200\r\n" +
            "\r\n"
        );
    }

    @After
    public void resetsStandardErrAndStandardOut() {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
    }

    
    //Helpers
    //Matches regex to string case insensitive
    //And allows for partial matching
    private boolean caseInsensitiveMatch(String a, String regex) {
        return Pattern.compile(".*" + regex + ".*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(a).matches();
    }

    //Sets a ByteArrayOutputStream to Standard Err and returns
    private ByteArrayOutputStream setNewErr() {
        ByteArrayOutputStream prelim = new ByteArrayOutputStream();
        System.setErr(new PrintStream(prelim));
        return prelim;
    }
    
    //Sets a ByteArrayOutputStream to Standard Out and returns
    private ByteArrayOutputStream setNewOut() {
        ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(newOut));
        return newOut;
    }

    //Passes string into a BufferedReader
    private BufferedReader parseResponse(String response) {
        return new BufferedReader(new StringReader(response));
    }
}
