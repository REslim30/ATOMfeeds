package main.java.http;

import java.io.*;
import java.net.*;

/**
 * HTTPResponseWriter
 * Writes a HTTP response to a buffered output stream.
 * Assumes HTTP 1.1 and is working with application/atom+xml; charset=utf-8
 */
public class HTTPResponseWriter {
    private PrintWriter out;

    public HTTPResponseWriter(PrintWriter out) {
        this.out = out;
    }
    
    public void writeResponse(int statusCode, String body, long lamportClock) {
        out.print("HTTP/1.1 " + Integer.toString(statusCode) + " " + getStatusMsg(statusCode) + "\r\n"); 
        out.print("Content-Type: application/atom+xml" + "\r\n");
        out.print("Content-Length: " + Integer.toString(body.length()) + "\r\n");
        out.print("Charset: utf-8" + "\r\n");
        out.print("Lamport-Clock: " + Long.toString(lamportClock) + "\r\n");
        out.print("Connection: keep-alive" + "\r\n");
        out.print("\r\n");
        out.print(body);
        out.flush();
    }

    private static String getStatusMsg(int statusCode) {
        switch (statusCode) {
            case 200: return "OK";
            case 201: return "Created";
            case 204: return "No Content";
            case 400: return "Bad Request";
            case 404: return "Not Found";
            case 411: return "Length Required";
            case 500: return "Internal Error";
            case 501: return "Not Implemented";

            default:  throw new RuntimeException("HTTPResponseWriter was given a unknown status code:" + Integer.toString(statusCode));
        }
    }
}
