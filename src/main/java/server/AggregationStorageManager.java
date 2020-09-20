package server;

import java.io.*;
import java.util.Date;

import java.sql.*;

/**
 * AggregationStorageManager
 */
public class AggregationStorageManager {
    //Gets a connection from the database
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + 
                new File("src/main/resources/server/aggregation.db")
                    .getAbsolutePath()
                        .toString());
    }
    

    //Saves a feed in SQL database
    //Returns true if inserted a new feed
    //Returns false if updated an old feed
    public synchronized boolean saveFeed(String ipPort, String body) throws SQLException {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM feeds WHERE ip_port=?");

            //First checks if row exists
            statement.setString(1, ipPort);
            boolean isOld;
            try (ResultSet result = statement.executeQuery()) {
                isOld = result.next();
            }
            

            //Then updates/inserts new feed
            long curTime = new Date().getTime();
            if (isOld) {
                statement = connection.prepareStatement("UPDATE feeds SET body=?, date=? WHERE ip_port=?");
                statement.setString(1, body);
                statement.setLong(2, curTime);
                statement.setString(3, ipPort);
                statement.executeUpdate();

                statement.close();
                return false;
            } else {
                statement = connection.prepareStatement("INSERT into feeds values(?,?,?)");
                statement.setString(1, ipPort);
                statement.setLong(2, curTime);
                statement.setString(3, body);
                statement.executeUpdate(); 

                statement.close();
                return true;
            }
        }
    }

    //Returns a feed in the SQL database
    //Returns all feeds in a string delimited by \r\n (carriage return and newline)
    public synchronized String retrieveAllFeeds() throws SQLException {
        try (Connection connection = getConnection()) {
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

    //Deletes 12 second old feeds
    public synchronized void deleteOldFeeds() throws SQLException {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            //Get the time of 12 seconds ago
            long oldTime = (new Date()).getTime() - (12*1000);
            statement.executeUpdate("DELETE from feeds WHERE date <= " + oldTime);
        }
    }

}
