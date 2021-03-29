package content;

import content.ContentServer;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.After;

import java.io.*;
import java.util.regex.*;

/**
 * ContentServerTest
 *
 * Integration tests for ContentServerTest.
 * More fine grained tests are available as unit tests (for various utiltiy helpers).
 * These tests just make sure all components work together smoothly and errors are logged
 */
public class ContentServerTest extends ContentServer {

    @Test(expected = IOException.class)
    public void throwsErrorIfInvalidHTTP() throws IOException {
        //No crlf to teerminate HTTP header
        receiveResponse(parseResponse(
            "HTTP/1.1 200 OK\r\n"   
        ));
    }

    @Test
    public void logsResponseIfValid() throws IOException {
        ByteArrayOutputStream out = setNewOut();
        receiveResponse(parseResponse(
            "HTTP/1.1 200 OK\r\n" +
            "Content-Length: 20\r\n" +
            "\r\n" + 
            "bodyishere1234567890"
        ));

        assertEquals(true, caseInsensitiveMatch(out.toString(), "200"));
        assertEquals(true, caseInsensitiveMatch(out.toString(), "OK"));
        assertEquals(true, caseInsensitiveMatch(out.toString(), "bodyishere1234567890"));
    }

    @Test
    public void setsLamportClock() throws IOException {
        ByteArrayOutputStream out = setNewOut();
        receiveResponse(parseResponse(
            "HTTP/1.1 200 OK\r\n" +
            "Lamport-Clock: 350\r\n" +
            "\r\n" 
        ));

        assertEquals(lamportClock, 350);
    }


    @Test
    //Test text files are in src/test/content
    public void throwsIOExceptionOnInvalidText() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream newErr = setNewErr();
        sendRequest(new PrintWriter(out), "hostname", "no_id.txt");

        assertEquals(true, caseInsensitiveMatch(newErr.toString(), "IOException"));
        assertEquals(true, out.toString().isEmpty());
    }

    @Test
    public void printsBasicText() {
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        sendRequest(new PrintWriter(out), "hostname", "entry.txt");

        assertEquals(out.toString(),
            "PUT /atom.xml HTTP/1.1\r\n" +
            "Host: hostname\r\n" +
            "User-Agent: ATOMContentServer/1/0\r\n" +
            "Connection: keep-alive\r\n" +
            "Content-Type: application/atom+xml\r\n" +
            "Content-Length: " + 365 + "\r\n" + 
            "Lamport-Clock: " + Long.toString(lamportClock) + "\r\n" + 
            "\r\n" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><feed xml:lang=\"en-US\" xmlns=\"https://www.w3.org/2005/Atom\"><title>test</title><updated>2007-03-25T03:49:00Z</updated><id>tag:starling.us,2007-02-15:/starling.us/starling_us_atom.xml</id><author><name>steve</name></author><entry><title>entry title</title><updated>2007-03-25T03:49:00Z</updated><id>sdflakjdf</id></entry></feed>"
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
