package atom;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.SAXException;

/**
 * AtomParser
 * Parses in an Atom Document as per
 * RFC-4287 (With modifications as per assignment spec)
 * Assumes UTF-8 encoding
 * Throws ParseExceptions on invalid atom:elements
 */
public class AtomParser {
    DocumentBuilder builder;
    InputStream input;
    Document doc;
    boolean hasFeedAuthor = false;

    public AtomParser(String body) {
        try {
            input = new ByteArrayInputStream(body.getBytes("UTF-8"));
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (UnsupportedEncodingException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } catch (ParserConfigurationException pe) {
            System.err.println(pe.getMessage());
            pe.printStackTrace();
        }
    }

    public void parseAtom() throws IOException, SAXException, InvalidAtomException {
        doc = builder.parse(input);
        doc.getDocumentElement().normalize();

        verifyAtom();
    }

    private void verifyAtom() throws SAXException, InvalidAtomException {
        Element root = doc.getDocumentElement();
        if (!root.getTagName().equals("feed"))
            throw new InvalidAtomException("Expected feed element instead of: " + root.getTagName());

        //Verifies feed
        boolean hasTitle = false;
        boolean hasId = false;
        boolean hasUpdated = false;
        boolean hasSubtitle = false;
        NodeList children = root.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node child = children.item(i);
            switch (child.getNodeName()) {
                case "title":
                    if (hasTitle)
                        throw new InvalidAtomException("Feed element must only contain exactly one title.");
                    hasTitle = true;
                    break;
                case "id":
                    if (hasId)
                        throw new InvalidAtomException("Feed element must only contain exactly one id.");
                    hasId = true;
                    break;
                case "updated":
                    if (hasUpdated)
                        throw new InvalidAtomException("Feed element must only contain exactly one updated element.");
                    hasUpdated = true;
                    break;
                case "subtitle":
                    if (hasSubtitle)
                        throw new InvalidAtomException("Feed element must have at most one subtitle.");
                    hasSubtitle = true;
                    break;
                case "#text":
                    break;
                case "link":
                    break;
                case "author":
                    hasFeedAuthor = true;
                    verifyAuthor(child);
                    break;
                case "entry":
                    verifyEntry(child);
                    break;
                default:
                    throw new InvalidAtomException("Unknown Feed Element: " + child.getNodeName());
            }
        }

        if (!hasTitle | !hasId | !hasUpdated)
            throw new InvalidAtomException("Feed element is missing title, id.");

    }

    private void verifyEntry(Node entry) throws InvalidAtomException {
        NodeList childNodes = entry.getChildNodes();

        boolean hasTitle = false;
        boolean hasId = false;
        boolean hasUpdated = false;
        boolean hasSummary = false;
        boolean hasAuthor = false;
        for (int i=0; i<childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            switch (child.getNodeName()) {
                case "#text":
                    break;
                case "link":
                    break;
                case "title":
                    if (hasTitle)
                        throw new InvalidAtomException("Entry must only have one title.");
                    hasTitle = true;
                    break;
                case "id":
                    if (hasId)
                        throw new InvalidAtomException("Entry must only have one Id.");
                    hasId = true;
                    break;
                case "updated":
                    if (hasUpdated)
                        throw new InvalidAtomException("Entry must only have one updated element.");
                    hasUpdated = true;
                    break;
                case "summary":
                    if (hasSummary)
                        throw new InvalidAtomException("Entry must only have at most summary element.");
                    hasSummary = true;
                    break;
                case "author":
                    hasAuthor = true;
                    verifyAuthor(child);
                    break;
            }
        }

        if (!hasTitle | !hasId | !hasUpdated) 
            throw new InvalidAtomException("Entry is missing title, id.");

        if (!hasFeedAuthor && !hasAuthor) 
            throw new InvalidAtomException("Entry must have author if feed does not have author.");
    }

    //Checks that Author's have the right elements
    private void verifyAuthor(Node author) throws InvalidAtomException {
        NodeList childNodes = author.getChildNodes();

        boolean hasName = false;
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            switch(child.getNodeName()) {
                case "name":
                    hasName = true;
                    break;
                case "#text":
                    break;
                default:
                    throw new InvalidAtomException("Unknown Author element: " + child.getNodeName());
            }
        }
        if (!hasName)
            throw new InvalidAtomException("Author must have a name");
    }


    //Gets a representation of the Feed
    //Returns null if Document is unitilized
    //Otherwise returns a human readable version
    //of feed
    public String getPrettyFeed() {
        if (doc == null) 
            return null;

        StringBuilder feedBuilder = new StringBuilder();
        Element feed = doc.getDocumentElement();
        feedBuilder.append("***Feed***\n");

        NodeList children = feed.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node child = children.item(i);
            switch (child.getNodeName()) {
                case "entry":
                    parsePrettyEntry(feedBuilder, child);
                    break;
                case "#text":
                    break;
                case "author":
                    feedBuilder.append("author: " + ((Element)child).getElementsByTagName("name").item(0).getTextContent() + "\n"); 
                    break;
                case "link":
                    feedBuilder.append("link: " + ((Element)child).getAttribute("href") + "\n"); 
                    break;
                default:
                    feedBuilder.append(child.getNodeName() + ": " + child.getTextContent() + "\n");
                    break;
            }
        }

        return feedBuilder.toString();    
    }

    void parsePrettyEntry(StringBuilder feedBuilder, Node entry) {
        feedBuilder.append("\n\t***Entry***\n");

        NodeList children = entry.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node child = children.item(i);
            switch (child.getNodeName()) {
                case "author":
                    feedBuilder.append("\tauthor: " + ((Element)child).getElementsByTagName("name").item(0).getTextContent() + "\n"); 
                    break;
                case "#text":
                    break;
                case "link":
                    feedBuilder.append("\tlink: " + ((Element)child).getAttribute("href") + "\n"); 
                    break;
                default:
                    feedBuilder.append("\t" + child.getNodeName() + ": " + child.getTextContent() + "\n");
                    break;
            }
        }
    }
}
