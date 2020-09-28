package server;

import server.AggregationStorageManager;

import static org.junit.Assert.assertEquals;
import org.junit.*;

import java.net.URL;
import java.nio.file.*;
import java.io.*;

import java.sql.*;

/**
 * AggregationStorageManagerSlowTest
 * 
 * Tests for the AggregationStorageManager. Specifically ones that require explicit waiting
 * to verify. (E.g. waiting 12 seconds after a PUT request for deletion)
 */
public class AggregationStorageManagerSlowTest {
    private final static String file = new File("src/main/resources/server/aggregation.db").getAbsolutePath().toString();
    Connection connection = null;
    Statement statement = null;
    AggregationStorageManager storage;

    @Before
    public void getConnection() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + file);
        statement = connection.createStatement();
        storage = new AggregationStorageManager();

        statement.executeUpdate("delete from feeds");
    }
    @Test
    public void deletesFiveOldFeed() throws SQLException, InterruptedException {
        storage.saveFeed("3:4:5::::6:8:5:8:f8c:8000", "test body");
        storage.saveFeed("/128.169.102.1", "test body");
        storage.saveFeed("id_string", "test body");
        storage.saveFeed("#@!id string is here", "test body");
        storage.saveFeed("ipipip***&&&string ^^_}{", "test body");
        storage.saveFeed("                        ", "test body");
        Thread.sleep(12*1000);
        storage.deleteOldFeeds();
        ResultSet resultSet = statement.executeQuery("select * from feeds");
        assertEquals(false, resultSet.next());
    }

    @Test
    public void doesntDeleteFiveOldFeeds() throws SQLException, InterruptedException { 
        storage.saveFeed("3:4:5::::6:8:5:8:f8c:8000", "test body");
        storage.saveFeed("/128.169.102.1", "test body");
        storage.saveFeed("id_string", "test body");
        storage.saveFeed("#@!id string is here", "test body");
        storage.saveFeed("ipipip***&&&string ^^_}{", "test body");
        storage.saveFeed("                        ", "test body");
        Thread.sleep(11*1000);
        storage.deleteOldFeeds();
        ResultSet resultSet = statement.executeQuery("select * from feeds");
        for (int i = 0; i < 6; i++) {
            assertEquals(true, resultSet.next());
        }
        assertEquals(false, resultSet.next());
    }

    @Test
    public void onlyDeletesOldFeeds() throws SQLException, InterruptedException {
        storage.saveFeed("3:4:5::::6:8:5:8:f8c:8000", "test body");
        storage.saveFeed("/128.169.102.1", "test body");
        storage.saveFeed("id_string", "test body");
        Thread.sleep(5*1000);
        storage.saveFeed("#@!id string is here", "test body");
        storage.saveFeed("ipipip***&&&string ^^_}{", "test body");
        storage.saveFeed("                        ", "test body");
        Thread.sleep(7*1000);
        storage.deleteOldFeeds();
        ResultSet resultSet = statement.executeQuery("select * from feeds");
        for (int i = 0; i < 3; i++) {
            assertEquals(true, resultSet.next());
        }
        assertEquals(false, resultSet.next());
    }

    @After
    public void clearsTable() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("DELETE FROM feeds");

        connection.close();
    }
}
