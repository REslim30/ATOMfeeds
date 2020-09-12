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
