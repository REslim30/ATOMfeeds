package main.java.server;

import java.nio.file.*;
import java.io.*;
import java.util.Date;

import java.sql.*;

/**
 * AggregationStorageManager
 */
public class AggregationStorageManager {
    //where server places all the files.
    //Assumes java is being called within the
    //topmost parent directory
    private final static String file = new File("src/main/resources/server/aggregation.db").getAbsolutePath().toString();
    

    public static synchronized void saveFeed(String ipPort, String body) throws SQLException {
        Connection connection = null;
        // create a database connection
        connection = DriverManager.getConnection("jdbc:sqlite:" + file);
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);  // set timeout to 30 sec.


        //First checks if row exists
        ResultSet result = statement.executeQuery("SELECT * FROM feeds WHERE ip_port='" + ipPort + "'");
        String curTime = Long.toString((new Date()).getTime());
        //Then updates/inserts new feed
        if (result.next()) {
            statement.executeUpdate("UPDATE feeds SET body = '" + body + "', date=" + curTime + " WHERE ip_port='" + ipPort + "'");
        } else {
            statement.executeUpdate("INSERT into feeds values('" + ipPort + "'," + curTime + ",'" + body + "')"); 
        }

        if (connection != null)
            connection.close();
        
    }

    public static synchronized String retrieveAllFeeds(long lamportClock) {
        return "";
    }
}
