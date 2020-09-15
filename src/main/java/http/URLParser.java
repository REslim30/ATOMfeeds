package http;

import java.net.URL;
import java.net.MalformedURLException;

//Class that holds URL parsing methods
public class URLParser {
    //Passes URL from the formats specified in assignment spec
    public static URL parseURL(String input) {
        URL url = null;
        try {
            if (input.matches("^http://.*")) {
                url = new URL(input);
            } else {
                url = new URL("http://" + input);
            }
        } catch (MalformedURLException urlE) {
            System.err.println("Please enter a URL in the following format: ");
            System.err.println("<host>:<port>");
            System.err.println("http://<host>:<port>");
            System.err.println(urlE.toString());
            System.exit(0);
        }
        return url;
    }
}

