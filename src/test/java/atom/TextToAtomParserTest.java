package test.java.atom;

import java.io.*;
import main.java.atom.TextToAtomParser;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * TextToAtomParserTest
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
        String result = readFile("no_title.txt");
    }

    //TODO: Test for every possible character

    @Test
    public void parsesLink() throws IOException {
        String result = readFile("link.txt");
        assertEquals("title", true, result.contains("<title>generic Title</title>"));
        assertEquals("updated", true, result.contains("<updated>wowsers</updated>"));
        assertEquals("id", true, result.contains("<id>5%412934871239</id>"));
        assertEquals("link", true, result.contains("<link href=\"http://starling.us/cgi-bin/gus_atom_xsl.pl?atom=starling_us_atom.xml!sort_order=Category\"></link>"));
        assertEquals("author", true, result.contains("<author><name>baker's delight</name></author>"));
    }


    private String readFile(String fileName) throws IOException {
        BufferedReader buffered = new BufferedReader(
            new InputStreamReader(
                ClassLoader
                    .getSystemClassLoader()
                        .getResourceAsStream("atom/" + fileName)
            )
        );
        TextToAtomParser parser = new TextToAtomParser(buffered);
        return parser.parseAtom();
    }
}
