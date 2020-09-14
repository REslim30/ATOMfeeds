package main.java.atom;

import java.io.*;

/**
 * TextToAtomParser
 * Intializes a buffered input stream and returns
 * an ATOM document as per RFC-4287
 * Assumes each text file is formatted like:
 * <feedElement>*(entry\n<entryElement>*)*
 *
 * Where <feedElement> is:
 * (title|subtitle|link|updated|author|name|id):<value>\n
 *
 * Where <entryElement> is:
 * (
 *
 *
 * Also assumes that the contents of each element is text (not html nor xhtml).
 * The parse is also case sensitive.
 * If any syntax errors or atom specification errors occur, parseAtom
 * will throw an IOException. 
 */

public class TextToAtomParser {
    BufferedReader in;
    StringBuilder atomBuilder;

    public TextToAtomParser(BufferedReader in) {
        this.in = in;
        atomBuilder = new StringBuilder();
    }

    //Parses the input stream into atom
    //Returns null if invalid Atom document
    //otherwise returns Atom document
    public String parseAtom() throws IOException {
        atomBuilder.append("<?xml version=\"1.0\" encoding\"UTF-8\"?>");
        parseFeed();
        return atomBuilder.toString();
    }

    //Parses atom:feed
    private void parseFeed() throws IOException {
        atomBuilder.append("<feed xml:lang=\"en-US\" xmlns=\"https://www.w3.org/2005/Atom\">");
        boolean hasTitle = false;
        boolean hasId = false;
        boolean hasUpdated = false;

        String line;
        String[] keyValuePair = new String[2];
        while ((line = in.readLine()) != null) {
            int delim = line.indexOf(':');
            if (delim == -1)
                break;
            keyValuePair[0] = line.substring(0, delim);
            keyValuePair[1] = line.substring(delim + 1);

            switch (keyValuePair[0]) {
                case "title":
                    if (hasTitle)
                        throw new IOException("RFC-4287: A feed MUST only contain one title element.");
                    hasTitle = true;
                    parseTextElement("title", keyValuePair[1]);
                    break;
                case "updated":
                    if (hasUpdated)
                        throw new IOException("RFC-4287: A feed MUST only contain one updated element.");
                    hasUpdated = true;
                    parseTextElement("updated", keyValuePair[1]);
                    break;
                case "id":
                    if (hasId)
                        throw new IOException("RFC-4287: A feed MUST only contain one id element.");
                    parseTextElement("id", keyValuePair[1]);
                    break;
                case "subtitle":
                    parseTextElement("subtitle", keyValuePair[1]);
                    break;
                case "author":
                    parseAuthor(keyValuePair[1]);
                    break;
                case "link":
                    parseLink(keyValuePair[1]);
                    break;
                default:
                    throw new IOException("Unknown atom element: " + keyValuePair[0]);
            }
        }
        

        if (line != null) {
            if (line.equals("entry")) {
                //parseEntry()
            } else {
                throw new IOException("Unknown text element: " + line);
            }
        }
        
        atomBuilder.append("</feed>");
    }

    //Parses an atom:author 
    private void parseAuthor(String value) {
        atomBuilder.append("<author>");
        parseTextElement("name", value);
        atomBuilder.append("</author>");
    }

    //Parses an atom:link
    private void parseLink(String value) {
        atomBuilder.append("<link href=\"" + value + "\">");
        atomBuilder.append("</link>");
    }

    //Generic Parser
    //Parses a type="text" atom element
    private void parseTextElement(String element, String value) {
        atomBuilder.append("<" + element + ">");
        atomBuilder.append(value);
        atomBuilder.append("</" + element + ">");
    }
}
