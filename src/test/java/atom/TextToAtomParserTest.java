package test.java.atom;

import java.io.*;
import main.java.atom.TextToAtomParser;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * TextToAtomParserTest
 * Tests for the TextToAtomParser object.
 * Note: A tradeoff was made between using String.contains
 * and strict equals. Using String.contains means that
 * there is a chance of a false positive however it gives 
 * room for implementation changes.
 */
public class TextToAtomParserTest {

    @Test
    public void parsesMinimum() throws IOException {
        String result = readFile("minimum.txt");
        assertEquals("title",true, result.contains("<title>test</title>"));
        assertEquals("updated",true, result.contains("<updated>2007-03-25T03:49:00Z</updated>"));
        assertEquals("id", true, result.contains("<id>tag:starling.us,2007-02-15:/starling.us/starling_us_atom.xml</id>"));
        assertEquals("author", true, result.contains("<author><name>steve</name></author>"));
    }

    @Test(expected = IOException.class)
    public void throwsExceptionOnNoTitle() throws IOException {
        readFile("no_title.txt");
    }

    @Test(expected = IOException.class)
    public void throwsExceptionOnNoUpdated() throws IOException {
        readFile("no_updated.txt");
    }

    @Test(expected = IOException.class)
    public void throwsExceptionOnNoId() throws IOException {
        readFile("no_id.txt");
    }

    @Test(expected = IOException.class)
    public void throwsExceptionOnDuplicateTitle() throws IOException {
        readFile("duplicate_title.txt");
    }

    @Test(expected = IOException.class)
    public void throwsExceptionOnDuplicateId() throws IOException {
        readFile("duplicate_id.txt");
    }

    @Test(expected = IOException.class)
    public void throwsExceptionOnDuplicateUpdated() throws IOException {
        readFile("duplicate_updated.txt");
    }

    @Test(expected = IOException.class)
    public void throwsExceptionOnDuplicateSubtitle() throws IOException {
        readFile("duplicate_subtitle.txt");
    }

    @Test
    public void parsesSpecialCharacters() throws IOException {
        String result = readFile("special_characters.txt");
        assertEquals("author",true, result.contains("<author><name><>?:\"{}}+_)(*&^%$#@!~`[]\\;',./</name></author>"));
        assertEquals("title",true, result.contains("<title><>?:\"{}}+_)(*&^%$#@!~`[]\\;',./</title>"));
        assertEquals("updated",true, result.contains("<updated><>?:\"{}}+_)(*&^%$#@!~`[]\\;',./</updated>"));
        assertEquals("id",true, result.contains("<id><>?:\"{}}+_)(*&^%$#@!~`[]\\;',./</id>"));
        assertEquals("subtitle",true, result.contains("<subtitle><>?:\"{}}+_)(*&^%$#@!~`[]\\;',./</subtitle>"));
        assertEquals("link",true, result.contains("<link href=\"<>?:\"{}}+_)(*&^%$#@!~`[]\\;',./\"></link>"));
    }


    @Test
    public void parsesLink() throws IOException {
        String result = readFile("link.txt");
        assertEquals("title", true, result.contains("<title>generic Title</title>"));
        assertEquals("updated", true, result.contains("<updated>wowsers</updated>"));
        assertEquals("id", true, result.contains("<id>5%412934871239</id>"));
        assertEquals("link", true, result.contains("<link href=\"http://starling.us/cgi-bin/gus_atom_xsl.pl?atom=starling_us_atom.xml!sort_order=Category\"></link>"));
        assertEquals("author", true, result.contains("<author><name>baker's delight</name></author>"));
    }

    // ***Entry testing***
    @Test
    public void parsesSingleEntry() throws IOException {
        String result = readFile("entry.txt");
        assertEquals("title", true, result.contains("<title>entry title</title>"));
        assertEquals("updated", true, result.contains("<updated>2007-03-25T03:49:00Z</updated>"));
        assertEquals("id", true, result.contains("<id>sdflakjdf</id>"));
        assertEquals("entry", true, result.contains("<entry>") && result.contains("</entry>"));
    }

    @Test
    public void parsesThreeEntries() throws IOException {
        String result = readFile("3_entries.txt");
        assertEquals("1-title", true, result.contains("<title>first entry</title>"));
        assertEquals("1-updated", true, result.contains("<updated>2007-03-25T03:49:00Z</updated>"));
        assertEquals("1-id", true, result.contains("<id>different entry</id>"));
        assertEquals("1-entry", true, result.contains("<entry>") && result.contains("</entry>"));

        assertEquals("2-title", true, result.contains("<title>second entry</title>"));
        assertEquals("2-updated", true, result.contains("<updated>2007-20-25T03:49:00Z</updated>"));
        assertEquals("2-id", true, result.contains("<id>876</id>"));

        assertEquals("3-title", true, result.contains("<title>third entry</title>"));
        assertEquals("3-updated", true, result.contains("<updated>2007-30-25T03:49:00Z</updated>"));
        assertEquals("3-id", true, result.contains("<id>456</id>"));
    }

    @Test
    public void allEntryElementsWithSpecialCharacters() throws IOException {
        String result = readFile("entry_all_elements.txt");

        assertEquals("title", true, result.contains("<title><?>?:\"{}|_)(*&^%$#@!C~`,./;'[]\\</title>"));
        assertEquals("updated", true, result.contains("<updated>2007-20-25T03:49:00Z<?>?:\"{}|_)(*&^%$#@!C~`,./;'[]\\</updated>"));
        assertEquals("id", true, result.contains("<id>876<?>?:\"{}|_)(*&^%$#@!C~`,./;'[]\\</id>"));
        assertEquals("author1", true, result.contains("<author><name>Steve hooligan<?>?:\"{}|_)(*&^%$#@!C~`,./;'[]\\</name></author>"));
        assertEquals("author2", true, result.contains("<author><name>different hooligan<?>?:\"{}|_)(*&^%$#@!C~`,./;'[]\\</name></author>"));
        assertEquals("summary", true, result.contains("<summary>Here's a useful summary kjp<?>?:\"{}|_)(*&^%$#@!C~`,./;'[]\\</summary>"));
        assertEquals("link1", true, result.contains("<link href=\"<?>?:\"{}|_)(*&^%$#@!C~`,./;'[]\\\"></link>"));
        assertEquals("link2", true, result.contains("<link href=\"Different Link<?>?:\"{}|_)(*&^%$#@!C~`,./;'[]\\\"></link>"));

    }

    @Test(expected = IOException.class)
    public void throwsExceptionOnDuplicateEntryId() throws IOException {
        readFile("entry_duplicate_id.txt");
    }

    @Test(expected = IOException.class)
    public void throwsExceptionOnDuplicateEntryTitle() throws IOException {
        readFile("entry_duplicate_title.txt");
    }

    @Test(expected = IOException.class)
    public void throwsExceptionOnDuplicateEntryUpdated() throws IOException {
        readFile("entry_duplicate_updated.txt");
    }

    @Test(expected = IOException.class)
    public void throwsExceptionOnDuplicateEntrySummary() throws IOException {
        readFile("entry_duplicate_summary.txt");
    }

    @Test(expected = IOException.class)
    public void throwsNoExceptionOnNoAuthorWhenFeedNoAuthor() throws IOException {
        readFile("entry_no_author.txt");
    }

    @Test
    public void throwsNoExceptionOnAllAuthorWhenFeedNoAuthor() throws IOException {
        readFile("entry_all_authors.txt");
    }

    @Test(expected = IOException.class)
    public void throwsExceptionOnEntryNoId() throws IOException {
        readFile("entry_no_id.txt");
    }

    @Test(expected = IOException.class)
    public void throwsExceptionOnEntryNoUpdated() throws IOException {
        readFile("entry_no_updated.txt");
    }

    @Test(expected = IOException.class)
    public void throwsExceptionOnEntryNoTitle() throws IOException {
        readFile("entry_no_title.txt");
    }


    private String readFile(String fileName) throws IOException {
        BufferedReader buffered = new BufferedReader(
            new InputStreamReader(
                ClassLoader
                    .getSystemClassLoader()
                        .getResourceAsStream("atom/text_atom_parser/" + fileName)
            )
        );
        TextToAtomParser parser = new TextToAtomParser(buffered);
        return parser.parseAtom();
    }
}
