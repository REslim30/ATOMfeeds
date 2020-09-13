package test.java.server;

import main.java.server.AggregationStorageManager;

import static org.junit.Assert.assertEquals;
import org.junit.*;

import java.net.URL;
import java.nio.file.*;
import java.io.*;

import java.sql.*;

/**
 * AggregationStorageManagerTest
 */
public class AggregationStorageManagerTest {
    private final static String file = new File("src/main/resources/server/aggregation.db").getAbsolutePath().toString();
    Connection connection = null;
    AggregationStorageManager storage;

    @Before
    public void getConnection() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + file);
        storage = new AggregationStorageManager();
    }

    @Test
    public void savesOneFeed() throws SQLException {
        storage.saveFeed("128.128.1.2:3000", "testing");

        Statement statement = connection.createStatement();
        ResultSet feeds = statement.executeQuery("SELECT * FROM feeds WHERE ip_port = '128.128.1.2:3000'");

        assertEquals(true, feeds.next());
        assertEquals("128.128.1.2:3000", feeds.getString("ip_port"));
        assertEquals("testing", feeds.getString("body"));
    }

    @Test
    public void updatesOneFeed() throws SQLException {
        storage.saveFeed("128.169.1.20:4000", "test body");
        storage.saveFeed("128.169.1.20:4000", "new body");

        Statement statement = connection.createStatement();
        ResultSet feeds = statement.executeQuery("SELECT * FROM feeds WHERE ip_port = '128.169.1.20:4000'");
        
        assertEquals(true, feeds.next());
        assertEquals("new body", feeds.getString("body"));
    }

    @Test
    public void getsOneFeed() throws SQLException {
        storage.saveFeed("128.169.1.20:4000", "\tnewbody\n\r\txmlgoeshere");
        String result = storage.retrieveAllFeeds();
        assertEquals( "\tnewbody\n\r\txmlgoeshere", result);
    }

    @Test
    public void getsTwoFeeds() throws SQLException {
        String firstBody = "second\t\n\r\r\r\r\r\rbodyworks or     not";
        String secondBody =  "\tnewbody\n\r\txmlgoeshere";

        storage.saveFeed("128.169.1.20:4000",firstBody);
        storage.saveFeed("136.169.1.20:5678",secondBody);
        String result = storage.retrieveAllFeeds();
        assertEquals(true, result.contains( "second\t\n\r\r\r\r\r\rbodyworks or     not"));
        assertEquals(true, result.contains( "\tnewbody\n\r\txmlgoeshere"));
        assertEquals(firstBody.length() + secondBody.length() + 2, result.length());
    }

    @After
    public void clearsTable() throws SQLException {
        Statement statement = connection.createStatement();

        statement.executeUpdate("DELETE FROM feeds");
        connection.close();
    }

}
