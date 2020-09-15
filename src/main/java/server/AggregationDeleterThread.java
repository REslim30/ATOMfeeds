package server;

import java.sql.*;

/**
 * AggregationDeleterThread
 */
public class AggregationDeleterThread extends Thread {
    private AggregationStorageManager storage;

    public AggregationDeleterThread(AggregationStorageManager storage) {
        this.storage = storage;
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("AggregationDeleterThread was interrupted");
                e.printStackTrace();
            }
            
            try {
                storage.deleteOldFeeds();
            } catch (SQLException e) {
                System.err.println("AggregationDeleterThread error while deleting feeds:");
                e.printStackTrace();
            }
        }
    }
}
