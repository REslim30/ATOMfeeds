package main.java.atom;

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
        Document doc = builder.parse(input);
        doc.getDocumentElement().normalize();

        verifyAtom(doc);
    }

    private void verifyAtom(Document doc) throws SAXException, InvalidAtomException {
        Element root = doc.getDocumentElement();
        if (!root.getTagName().equals("feed"))
            throw new InvalidAtomException("Expected feed element instead of: " + root.getTagName());

        //Verifies feed
        boolean hasTitle = false;
        NodeList children = root.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node child = children.item(i);
            switch (child.getNodeName()) {
                case "title":
                    if (hasTitle)
                        throw new InvalidAtomException("Feed element must only contain exactly one title.");
                    hasTitle = true;
                    break;
            }
        }

        if (!hasTitle)
            throw new InvalidAtomException("Feed element is missing title.");
    }
}
