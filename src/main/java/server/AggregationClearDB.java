package server;

import java.sql.*;
import java.io.*;

/**
 * AggregationClearDB
 *
 * A script to delete all feeds
 * useful for testing and initializing server
 */
public class AggregationClearDB {

    public static void main(String[] args) throws SQLException {
        System.out.println("Attempting to clear database");
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            statement.executeUpdate("DELETE from feeds");
            System.out.println("Successfully cleared database");
        } catch (SQLException sql) {
            System.err.println(sql.getMessage());
            sql.printStackTrace();
        }
    }  

    //Gets a connection from the database
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + 
                new File("src/main/resources/server/aggregation.db")
                    .getAbsolutePath()
                        .toString());
    }
}
