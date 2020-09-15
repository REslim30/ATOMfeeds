package http;

import http.URLParser;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import org.junit.Test;


/**
 * URLParserTest
 */
public class URLParserTest {
    @Test  
    public void passesStrippedURL() {
        URL url = URLParser.parseURL("localhost:3000");
        assertEquals(url.getHost(), "localhost");
        assertEquals(url.getPort(), 3000);
    }

    @Test
    public void passesHttpPrefix() {
        URL url = URLParser.parseURL("http://testurl:8901");
        assertEquals(url.getHost(), "testurl");
        assertEquals(url.getPort(), 8901);
    }

    @Test(expected = NullPointerException.class)
    public void throwsExceptionOnNull() {
        URL url = URLParser.parseURL(null);
    }
}
