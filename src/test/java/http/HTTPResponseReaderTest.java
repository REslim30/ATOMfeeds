package http;

import http.HTTPResponseReader;

import java.io.*;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * HTTPResponseReaderTest
 */
public class HTTPResponseReaderTest {

    @Test
    public void parses200() throws IOException {
        HTTPResponseReader response = passResponse(
            "HTTP/1.1 200 OK\r\n" +
            "\r\n"
        );

        assertEquals(200, response.getStatusCode());
        assertEquals("OK", response.getStatusMsg());
        assertEquals(null, response.getBody());
    }

    @Test
    public void parses400() throws IOException {
        HTTPResponseReader response = passResponse(
            "HTTP/1.1 400 Bad Request\r\n" + 
            "\r\n"
        );

        assertEquals(400, response.getStatusCode());
        assertEquals("Bad Request", response.getStatusMsg());
        assertEquals(null, response.getBody());
    }

    @Test
    public void parsesHeader() throws IOException {
        HTTPResponseReader response = passResponse(
            "HTTP/1.1 302 Redirect\r\n" + 
            "ALLCAPS: test_value\r\n" + 
            "nocaps: nocapValue1234\r\n" + 
            "connection: keep-alive\r\n" + 
            "Accept-Language: en-us\r\n" +
            "\r\n"
        );

        assertEquals("test_value", response.getHeader("allcaps"));
        assertEquals("nocapValue1234", response.getHeader("NOCaPS"));
        assertEquals("keep-alive", response.getHeader("CoNnEcTion"));
        assertEquals("en-us", response.getHeader("Accept-Language"));
        assertEquals(null, response.getHeader("doesnt_exist"));
    }

    @Test
    public void parsesBody() throws IOException {
        HTTPResponseReader response = passResponse(
            "HTTP/1.1 200 OK\r\n" +
            "Content-Length: 30\r\n" +
            "\r\n" + 
            "1234567890!@#$%^&*()-=_=,./;'["
        );

        assertEquals("1234567890!@#$%^&*()-=_=,./;'[", response.getBody());
    }

    @Test
    public void parsesOnlyContentLengthToBody() throws IOException {
        HTTPResponseReader response = passResponse(
            "HTTP/1.1 404 Not Found\r\n" + 
            "Content-Length: 10\r\n" +
            "\r\n" + 
            "1234567890Shouldn'tParseHere"
        );

        assertEquals("1234567890", response.getBody());
    }

    @Test(expected = IOException.class)
    public void ThrowsErrorIfNotEnoughTokensInStatusLine() throws IOException {
        passResponse(
            "HTTP/1.1 404\r\n" +
            "\r\n"
        );
    }

    @Test(expected = IOException.class)
    public void ThrowsErrorIfNoCRLF() throws IOException {
        passResponse(
            "HTTP/1.1 500 Server Error\r\n"
        );
    }

    @Test(expected = IOException.class)
    public void throwsErrorIfNotHTTP() throws IOException {
        passResponse(
            "RFC/1.2 200 OK\r\n" + 
            "\r\n"
        );
    }

    @Test(expected = IOException.class)
    public void throwsErrorIfTwoDigitStatusCode() throws IOException {
        passResponse(
            "HTTP/1.1 20 OK\r\n" +
            "\r\n"
        );
    }

    @Test(expected = IOException.class)
    public void throwsErrorIfTooManyDigits() throws IOException {
        passResponse(
            "HTTP/1.1 2129347890 OK\r\n" +
            "\r\n"
        );
    }

    @Test(expected = IOException.class)
    public void throwsErrorIfLetterStatusCode() throws IOException {
        passResponse(
            "HTTP/1.1 abc OK\r\n" +
            "\r\n"
        );
    }

    private HTTPResponseReader passResponse(String response) throws IOException {
        HTTPResponseReader httpResponse = new HTTPResponseReader(new BufferedReader(new StringReader(response)));
        httpResponse.readResponse();
        return httpResponse;
    }
}
