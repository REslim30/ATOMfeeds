package server;

import server.AggregationDeleterThread;
import server.AggregationStorageManager;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import static org.mockito.Mockito.*;


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

    @Test
    public void callsTwoTimesIn24Seconds() throws InterruptedException, SQLException {
        //Create mock instance of mockStorageManager
        AggregationStorageManager mockStorageManager = mock(AggregationStorageManager.class);

        //Start the thread
        AggregationDeleterThread thread = new AggregationDeleterThread(mockStorageManager);
        thread.start();

        Thread.sleep(26*1000);
        //In 26 seconds, mockStorageManager should have been called 2 times
        verify(mockStorageManager, times(2)).deleteOldFeeds();
    }

}
