package test.java.server;

import main.java.server.AggregationStorageManager;

import static org.junit.Assert.assertEquals;
import org.junit.*;

import java.net.URL;
import java.nio.file.*;
import java.io.*;

/**
 * AggregationStorageManagerTest
 */
public class AggregationStorageManagerTest {
    private final static Path serverResources = new File("src/main/resources/server").toPath();

    @Test
    public void savesOneFile() throws IOException {
        AggregationStorageManager.save(0,0,"body text");
        boolean resourceExists= serverResourceMatches("0_0.xml", "body text");
        assertEquals(true, resourceExists);
    }

    @Test
    public void savesTwoFiles() throws IOException {
        AggregationStorageManager.save(129038410,458,"crazy flkjasdl;fkjasd;flkjadsfasdl;fkjasl;df\nslkfjsldka\nfjdf");
        assertEquals(true, serverResourceMatches("129038410_458.xml", "crazy flkjasdl;fkjasd;flkjadsfasdl;fkjasl;df\nslkfjsldka\nfjdf"));
        AggregationStorageManager.save(129038420,458,"afjoinrqwerqw.ermn,.fasdf\nasldkfjas;ldkfj\nasdkjfa;lskdf\n");
        assertEquals(true, serverResourceMatches("129038420_458.xml", "afjoinrqwerqw.ermn,.fasdf\nasldkfjas;ldkfj\nasdkjfa;lskdf\n"));
    }

    @Test(expected = IOException.class)
    public void throwsExceptionIfDuplicateName() throws IOException {
        AggregationStorageManager.save(12,12,"");
        AggregationStorageManager.save(12,12,"");
    }

    @Test
    public void retrievesOneFile() throws IOException {
        AggregationStorageManager.save(12, 324138, "test body");
        assertEquals("test body", AggregationStorageManager.retrieve(12, 324138));
    }

    @Test
    public void retrievesEmptyIfLamportClockLess() throws IOException {
        AggregationStorageManager.save(12, 324138, "test body");
        assertEquals("", AggregationStorageManager.retrieve(11, 2394234)); 
    }

    @Test
    public void retrievesEmptyIfLamportClockEqualButConnectionIdLess() throws IOException {
        AggregationStorageManager.save(1000, 324138, "test body");
        assertEquals("", AggregationStorageManager.retrieve(1000, 300)); 
    }

    @Test
    public void retrievesIfLamportClockEqualButConnectionIdGreater() throws IOException {
        AggregationStorageManager.save(1000, 324138, "test body");
        assertEquals("test body", AggregationStorageManager.retrieve(1000, 400000)); 
    }

    @Test
    public void retrievesIfLamportClockEqualAndConnectionIdEqual() throws IOException {
        AggregationStorageManager.save(1000, 400000, "test body\n\nwowsers\nnewlines?\ttabs?\tthey should all work");
        assertEquals("test body\n\nwowsers\nnewlines?\ttabs?\tthey should all work", AggregationStorageManager.retrieve(1000, 400000)); 
    }

    @Test
    public void retrievesTwo() throws IOException {
        String s1 =  "test body\n\nwowsers\nnewlines?\ttabs?\tthey should all work";
        String s2 =  "very specific string";
        AggregationStorageManager.save(1000, 400000, s1);
        AggregationStorageManager.save(1500, 400000, s2);

        String body = AggregationStorageManager.retrieve(2000, 0);
        System.out.println(body);
        assertEquals(true, body.contains(s1));
        assertEquals(true, body.contains(s2));
        assertEquals(s1.length() + s2.length() + 2, body.length());
    }

    @Test
    public void saveTwoRetrievesOneIfLamportClockLess() throws IOException {
        String s1 =  "test body\n\nwowsers\nnewlines?\ttabs?\tthey should all work";
        String s2 =  "very specific string";
        AggregationStorageManager.save(1000, 400000, s1);
        AggregationStorageManager.save(1500, 400000, s2);

        String body = AggregationStorageManager.retrieve(1250, 40000);
        assertEquals(true, body.contains(s1));
        assertEquals(false, body.contains(s2));
        assertEquals(s1.length(), body.length());
    }

    @After
    public void purgeServerDirectory() {
        purgeDirectory(serverResources.toFile());
    }


    private boolean serverResourceMatches(String fileName, String body) {
        Path fullPath = Paths.get(serverResources.toString(), "/",fileName);
        if (!Files.exists(fullPath)) 
            return false;

        try {
            return body.equals(new String(Files.readAllBytes(fullPath)));
        } catch (Exception e) {
            System.err.println("serverResourceMatches error: " + e.toString());
            System.exit(0);
        }
        return false;
    }

    private void purgeDirectory(File dir) {
        for (File file: dir.listFiles()) {
            if (file.isDirectory())
                purgeDirectory(file);
            file.delete();
        }
    }
}
