package http;

import http.HTTPRequestReader;
import java.io.*;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * HTTPRequestReaderTest
 * 
 * Tests for the HTTPRequestReader class.
 */
public class HTTPRequestReaderTest {

    @Test
    public void passesBasicGet() throws IOException {
        HTTPRequestReader request = parseRequest(
            "GET /resource HTTP/1.1\r\n" + 
            "\r\n"
        );

        assertEquals("GET", request.getMethod());
        assertEquals("/resource", request.getURL());
    } 

    @Test(expected = IOException.class)
    public void throwsExceptionOnNonHTTPVersion() throws IOException {
        parseRequest(
            "GET /resource RFCT/1.1\r\n" + 
            "\r\n"
        );
    } 

    @Test(expected = IOException.class)
    public void throwsExceptionOnEmptyRequest() throws IOException {
        parseRequest("");
    } 

    @Test(expected = IOException.class)
    public void throwsExceptionTooManyStatusLineTokens() throws IOException {
        parseRequest(
            "GET /resource FVC/1.1 1234\r\n" + 
            "\r\n"
        );
    } 

    @Test(expected = IOException.class)
    public void throwsExceptionOnNoCRLF() throws IOException {
        parseRequest(
            "GET /resource HTTP/1.1\r\n" 
        );
    }

    @Test
    public void parsesHeaders() throws IOException {
        HTTPRequestReader request = parseRequest(
            "GET /resource HTTP/1.1\r\n" +
            "ALLCAPS: test_value\r\n" + 
            "nocaps: nocapValue1234\r\n" + 
            "connection: keep-alive\r\n" + 
            "Accept-Language: en-us\r\n" +
            "\r\n"
        );

        assertEquals("test_value", request.getHeader("allcaps"));
        assertEquals("nocapValue1234", request.getHeader("NOCaPS"));
        assertEquals("keep-alive", request.getHeader("CoNnEcTion"));
        assertEquals("en-us", request.getHeader("Accept-Language"));
        assertEquals(null, request.getHeader("doesnt_exist"));
    }

    @Test
    public void parsesBody() throws IOException {
        HTTPRequestReader request = parseRequest(
            "GET /resource HTTP/1.1\r\n" +
            "Content-Length: 30\r\n" +
            "\r\n" + 
            "1234567890!@#$%^&*()-=_=,./;'["
        );

        assertEquals("1234567890!@#$%^&*()-=_=,./;'[", request.getBody());
    }

    @Test
    public void parsesOnlyContentLengthToBody() throws IOException {
        HTTPRequestReader request = parseRequest(
            "GET /resource HTTP/1.1\r\n" +
            "Content-Length: 10\r\n" +
            "\r\n" + 
            "1234567890Shouldn'tParseHere"
        );

        assertEquals("1234567890", request.getBody());
    }

    private HTTPRequestReader parseRequest(String requestString) throws IOException {
        HTTPRequestReader request = new HTTPRequestReader(new BufferedReader(new StringReader(requestString)));
        request.readRequest();
        return request;
    }
}
