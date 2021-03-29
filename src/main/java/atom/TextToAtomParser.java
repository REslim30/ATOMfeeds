package atom;

import java.io.*;

/**
 * TextToAtomParser
 * Intializes a buffered input stream and returns
 * an ATOM document as per RFC-4287
 * Assumes each text file is formatted like:
 * <feedElement>*(entry\n<entryElement>*)*
 *
 * Where <feedElement> is:
 * (title|subtitle|link|updated|author|id):<value>\n
 *
 * Where <entryElement> is:
 * (title|summary|link|updated|author|id):<value>\n
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
    boolean hasFeedAuthor;

    public TextToAtomParser(BufferedReader in) {
        this.in = in;
        atomBuilder = new StringBuilder();
        hasFeedAuthor = false;
    }

    //Parses the input stream into atom
    //Returns null if invalid Atom document
    //otherwise returns Atom document
    public String parseAtom() throws IOException {
        atomBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        parseFeed();
        return atomBuilder.toString();
    }

    //Parses atom:feed
    private void parseFeed() throws IOException {
        atomBuilder.append("<feed xml:lang=\"en-US\" xmlns=\"https://www.w3.org/2005/Atom\">");
        boolean hasTitle = false;
        boolean hasId = false;
        boolean hasUpdated = false;
        boolean hasSubtitle = false;

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
                    hasId = true;
                    parseTextElement("id", keyValuePair[1]);
                    break;

                case "subtitle":
                    if (hasSubtitle)
                        throw new IOException("RFC-4287: A feed MUST contain at most one subtitle.");
                    hasSubtitle = true;
                    parseTextElement("subtitle", keyValuePair[1]);
                    break;

                case "author":
                    hasFeedAuthor = true;
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
                parseEntry();
            } else {
                throw new IOException("Unknown text element: " + line);
            }
        }

        if (!hasTitle | !hasUpdated | !hasId) 
           throw new IOException("RFC-4287: Missing title, updated or id element"); 
        
        atomBuilder.append("</feed>");
    }

    
    //Parses a atom:entry
    //Calls itself recursively on multiple entries
    private void parseEntry() throws IOException {
        atomBuilder.append("<entry>");

        boolean hasAuthor = false;
        boolean hasId = false;
        boolean hasTitle = false;
        boolean hasUpdated = false;
        boolean hasSummary = false;

        String line;
        String[] keyValuePair = new String[2];
        while ((line = in.readLine()) != null) {
            int delim = line.indexOf(':');
            if (delim == -1)
                break;

            keyValuePair[0] = line.substring(0, delim);
            keyValuePair[1] = line.substring(delim+1);

            switch (keyValuePair[0]) {
                case "author":
                    hasAuthor = true;
                    parseAuthor(keyValuePair[1]);
                    break;

                case "id":
                    if (hasId)
                        throw new IOException("RFC-4287: atom:entry must contain exactly one id");
                    hasId = true;
                    parseTextElement("id", keyValuePair[1]);
                    break;

                case "link":
                    parseLink(keyValuePair[1]);
                    break;

                case "title":
                    if (hasTitle)
                        throw new IOException("RFC-4287: atom:entry must contain exactly one title");
                    hasTitle = true;
                    parseTextElement("title", keyValuePair[1]);
                    break;

                case "updated":
                    if (hasUpdated)
                        throw new IOException("RFC-4287: atom:entry must contain exactly one updated element");
                    hasUpdated = true;
                    parseTextElement("updated", keyValuePair[1]);
                    break;

                case "summary":
                    if (hasSummary)
                        throw new IOException("RFC-4287: atom:entry must contain at most one summary");
                    hasSummary = true;
                    parseTextElement("summary", keyValuePair[1]);
                    break;

                default:
                    throw new IOException("Unknown text element: " + keyValuePair[0]);
            }
        }

        if (!hasFeedAuthor && !hasAuthor)
            throw new IOException("RFC-4287: atom:entry must contain an author if atom:feed does not contain an author.");

        if (!hasId || !hasTitle || !hasUpdated)
            throw new IOException("RFC-4286: atom:entry is missing an id, title or updated element");

        atomBuilder.append("</entry>");
        if (line != null) {
            if (line.equals("entry")) {
                parseEntry();
                return;            
            } else {
                throw new IOException("Unknown text element: " + line);
            }
        }
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
