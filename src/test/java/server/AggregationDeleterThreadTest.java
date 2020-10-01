package server;

import server.AggregationDeleterThread;
import server.AggregationStorageManager;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;

import static org.mockito.Mockito.*;
import java.io.*;


public class AggregationDeleterThreadTest {

    @Test
    public void callsZeroTimesInNoWait() throws InterruptedException, SQLException {
        //Create mock instance of mockStorageManager
        AggregationStorageManager mockStorageManager = mock(AggregationStorageManager.class);

        //Start the thread
        AggregationDeleterThread thread = new AggregationDeleterThread(mockStorageManager);
        thread.start();

        verify(mockStorageManager, never()).deleteOldFeeds();
    }

    // DeleterThread should query every second.
    // But we should allow for 1 second give or take
     @Test
    public void callsAtLeast5TimesIn6Seconds() throws InterruptedException, SQLException {
        //Create mock instance of mockStorageManager
        AggregationStorageManager mockStorageManager = mock(AggregationStorageManager.class);

        //Start the thread
        AggregationDeleterThread thread = new AggregationDeleterThread(mockStorageManager);
        thread.start();

        Thread.sleep(6*1000);
        //In 26 seconds, mockStorageManager should have been called 2 times
        verify(mockStorageManager, atLeast(5)).deleteOldFeeds();
    }

}
