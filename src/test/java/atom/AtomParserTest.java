package test.java.atom;

import main.java.atom.*;

import java.io.*;
import java.nio.file.Files;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import org.junit.Test;


/**
 * AtomParserTest
 * Mainly tests for checking valid
 * Atom. Involves a lot of Exception checking
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

    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnMissingFeedId() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("no_id.xml"));
        parser.parseAtom();
    }

    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnDuplicateFeedId() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("duplicate_id.xml"));
        parser.parseAtom();
    }

    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnMissingFeedUpdated() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("no_updated.xml"));
        parser.parseAtom();
    }

    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnDuplicateFeedUpdated() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("duplicate_updated.xml"));
        parser.parseAtom();
    }

    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnDuplicateFeedSubtitle() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("duplicate_subtitle.xml"));
        parser.parseAtom();
    }

    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnUnkownElement() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("unknown_element.xml"));
        parser.parseAtom();
    }

    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnAuthorWithoutName() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("author_no_name.xml"));
        parser.parseAtom();
    }

    // ***Entry Testing***    
    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnEntryWithoutTitle() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("entry_no_title.xml"));
        parser.parseAtom();
    }

    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnEntryDuplicateTitle() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("entry_duplicate_title.xml"));
        parser.parseAtom();
    }

    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnEntryWithoutId() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("entry_no_id.xml"));
        parser.parseAtom();
    }

    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnEntryDuplicateId() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("entry_duplicate_id.xml"));
        parser.parseAtom();
    }

    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnEntryWithoutUpdated() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("entry_no_updated.xml"));
        parser.parseAtom();
    }

    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnEntryDuplicateUpdated() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("entry_duplicate_updated.xml"));
        parser.parseAtom();
    }

    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnEntryDuplicateSummary() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("entry_duplicate_summary.xml"));
        parser.parseAtom();
    }

    @Test(expected = InvalidAtomException.class) 
    public void throwsExceptionOnEntryNoAuthorIfFeedNoAuthor() throws IOException, SAXException, InvalidAtomException {
        AtomParser parser = new AtomParser(readFile("entry_no_author.xml"));
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
