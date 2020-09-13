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

    @Test
    public void savesOneFeed() throws SQLException {
        AggregationStorageManager.save("128.128.1.2:3000", "testing");
    }

    @After
    public void clearsTable() throws SQLException {
        Statement statement = getConnection();

        statement.executeUpdate("DELETE FROM feeds");
    }

    private Statement getConnection() throws SQLException {
        Connection connection = null;
        connection = DriverManager.getConnection("jdbc:sqlite:" + file);
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);
        return statement;
    }
}
