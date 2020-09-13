package main.java.server;

import java.io.*;
import java.util.Date;

import java.sql.*;

/**
 * AggregationStorageManager
 */
public class AggregationStorageManager {
    //SQL connection
    private Connection connection = null;

    //When constructed, establishes connection with database
    //Assumes java is being called within the
    //topmost parent directory
    public AggregationStorageManager() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + 
                new File("src/main/resources/server/aggregation.db")
                    .getAbsolutePath()
                        .toString());
    }

    protected void finalize() {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            System.err.println("Error while destructing AggregationStorageManager object");
            e.printStackTrace();
        }
    }
    

    //Saves a feed in SQL database
    //Returns true if inserted a new feed
    //Returns false if updated an old feed
    public synchronized boolean saveFeed(String ipPort, String body) throws SQLException {
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);  // set timeout to 30 sec.

        //First checks if row exists
        ResultSet result = statement.executeQuery("SELECT * FROM feeds WHERE ip_port='" + ipPort + "'");
        String curTime = Long.toString((new Date()).getTime());
        //Then updates/inserts new feed
        if (result.next()) {
            statement.executeUpdate("UPDATE feeds SET body = '" + body + "', date=" + curTime + " WHERE ip_port='" + ipPort + "'");
            return false;
        } else {
            statement.executeUpdate("INSERT into feeds values('" + ipPort + "'," + curTime + ",'" + body + "')"); 
            return true;
        }
    }

    //Returns a feed in the SQL database
    //Returns all feeds in a string delimited by \r\n (carriage return and newline)
    public synchronized String retrieveAllFeeds() throws SQLException {
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);

        ResultSet allFeeds = statement.executeQuery("SELECT * FROM feeds"); 
        StringBuilder bodyBuilder = new StringBuilder();
        while (allFeeds.next()) {
            if (bodyBuilder.length() != 0)
                bodyBuilder.append("\r\n");
            bodyBuilder.append(allFeeds.getString("body"));
        }
        return bodyBuilder.toString();
    }
}
