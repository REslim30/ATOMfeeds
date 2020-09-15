package test.java.atom;

import main.java.atom.*;

import java.io.*;
import java.nio.file.Files;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import org.junit.Test;


/**
 * AtomParserTest
 */
public class AtomParserTest {

    //Throws exception on invalid XML
    @Test(expected = SAXException.class) 
    public void throwsExceptionOnInvalidXML() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser("invalid XML <>?>:L:L\"{}|+_)(*&^%$#@!~!@$");
        parser.parseAtom();
    }

    //Reads in basic atom
    @Test
    public void readsBasicXML() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("basic.xml"));
        parser.parseAtom();
    }

    //Invalid ATOM testing
    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnMissingFeedTag() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("no_feed.xml"));
        parser.parseAtom();
    }

    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnMissingFeedTitle() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("no_title.xml"));
        parser.parseAtom();
    }

    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnDuplicateFeedTitle() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("duplicate_title.xml"));
        parser.parseAtom();
    }
    
    //Assumes that java is being called from project root
    private String readFile(String fileName) throws IOException {
        return new String(
            Files.readAllBytes(
                new File("src/test/resources/atom/atom_parser/" + fileName)
                    .toPath()
            )
        );
    }
}
