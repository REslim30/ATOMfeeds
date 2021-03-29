package http;

import http.*;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.net.*;
import java.io.*;

/**
 * HTTPResponseWriterTest
 * 
 * Tests for the HTTPResponseWriter class
 */
public class HTTPResponseWriterTest {

    @Test
    public void writesAnEmptyRequest() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HTTPResponseWriter writer = setOutput(out);

        writer.writeResponse(200, null, 20);
        assertEquals(
            "HTTP/1.1 200 OK\r\n" + 
            "Content-Type: application/atom+xml\r\n" + 
            "Content-Length: 0\r\n" + 
            "Charset: utf-8\r\n" + 
            "Lamport-Clock: 20\r\n" + 
            "Connection: keep-alive\r\n" + 
            "\r\n",
            out.toString()
        );
    }

    @Test
    public void writesA201Response() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HTTPResponseWriter writer = setOutput(out);

        writer.writeResponse(201, null, 20);
        assertEquals(
            "HTTP/1.1 201 Created\r\n" + 
            "Content-Type: application/atom+xml\r\n" + 
            "Content-Length: 0\r\n" + 
            "Charset: utf-8\r\n" + 
            "Lamport-Clock: 20\r\n" + 
            "Connection: keep-alive\r\n" + 
            "\r\n",
            out.toString()
        );
    }

    @Test
    public void writesA204Response() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HTTPResponseWriter writer = setOutput(out);

        writer.writeResponse(204, null, 20);
        assertEquals(
            "HTTP/1.1 204 No Content\r\n" + 
            "Content-Type: application/atom+xml\r\n" + 
            "Content-Length: 0\r\n" + 
            "Charset: utf-8\r\n" + 
            "Lamport-Clock: 20\r\n" + 
            "Connection: keep-alive\r\n" + 
            "\r\n",
            out.toString()
        );
    }

    @Test
    public void writesA400Response() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HTTPResponseWriter writer = setOutput(out);

        writer.writeResponse(400, null, 20);
        assertEquals(
            "HTTP/1.1 400 Bad Request\r\n" + 
            "Content-Type: application/atom+xml\r\n" + 
            "Content-Length: 0\r\n" + 
            "Charset: utf-8\r\n" + 
            "Lamport-Clock: 20\r\n" + 
            "Connection: keep-alive\r\n" + 
            "\r\n",
            out.toString()
        );
    }

    @Test
    public void writesA500Response() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HTTPResponseWriter writer = setOutput(out);

        writer.writeResponse(500, null, 20);
        assertEquals(
            "HTTP/1.1 500 Internal Error\r\n" + 
            "Content-Type: application/atom+xml\r\n" + 
            "Content-Length: 0\r\n" + 
            "Charset: utf-8\r\n" + 
            "Lamport-Clock: 20\r\n" + 
            "Connection: keep-alive\r\n" + 
            "\r\n",
            out.toString()
        );
    }

    @Test
    public void writesA404Resonse() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HTTPResponseWriter writer = setOutput(out);

        writer.writeResponse(404, null, 20);
        assertEquals(
            "HTTP/1.1 404 Not Found\r\n" + 
            "Content-Type: application/atom+xml\r\n" + 
            "Content-Length: 0\r\n" + 
            "Charset: utf-8\r\n" + 
            "Lamport-Clock: 20\r\n" + 
            "Connection: keep-alive\r\n" + 
            "\r\n",
            out.toString()
        );
    }

    @Test(expected = RuntimeException.class)
    public void throwsErrorOnUnkownStatusCode() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HTTPResponseWriter writer = setOutput(out);

        writer.writeResponse(3000, null, 20); 
    }

    @Test
    public void writesABody() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HTTPResponseWriter writer = setOutput(out);

        writer.writeResponse(404, "ThisIsTheBody,./;'[]\\1234567890-=!@#$%^&*()_+,./:\"[]" , 20);
        assertEquals(
            "HTTP/1.1 404 Not Found\r\n" + 
            "Content-Type: application/atom+xml\r\n" + 
            "Content-Length: 52\r\n" + 
            "Charset: utf-8\r\n" + 
            "Lamport-Clock: 20\r\n" + 
            "Connection: keep-alive\r\n" + 
            "\r\n" + 
            "ThisIsTheBody,./;'[]\\1234567890-=!@#$%^&*()_+,./:\"[]",
            out.toString()
        );
    }

    //Takes an outputstream for HTTPResponseWriter to write to
    private HTTPResponseWriter setOutput(ByteArrayOutputStream out) {
        return new HTTPResponseWriter(new PrintWriter(out));
    }
}
