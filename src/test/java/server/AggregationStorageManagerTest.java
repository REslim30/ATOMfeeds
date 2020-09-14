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
    public void savesOneFeed() throws SQLException {
        storage.saveFeed("128.128.1.2:3000", "testing");

        ResultSet feeds = statement.executeQuery("SELECT * FROM feeds WHERE ip_port = '128.128.1.2:3000'");

        assertEquals(true, feeds.next());
        assertEquals("128.128.1.2:3000", feeds.getString("ip_port"));
        assertEquals("testing", feeds.getString("body"));
    }

    @Test
    public void savesSpecialCharacters() throws SQLException {
        storage.saveFeed("128.12039.12301203.1231:3000", "<>,.:\";'{}[]\\|+_=-)(*&^%$#@!~`/?");

        ResultSet feeds = statement.executeQuery("SELECT * FROM feeds WHERE ip_port = '128.12039.12301203.1231:3000'");

        assertEquals(true, feeds.next());
        assertEquals("<>,.:\";'{}[]\\|+_=-)(*&^%$#@!~`/?", feeds.getString("body"));
    }

    @Test
    public void updatesOneFeed() throws SQLException {
        storage.saveFeed("128.169.1.20:4000", "test body");
        storage.saveFeed("128.169.1.20:4000", "new body");

        ResultSet feeds = statement.executeQuery("SELECT * FROM feeds WHERE ip_port = '128.169.1.20:4000'");
        
        assertEquals(true, feeds.next());
        assertEquals("new body", feeds.getString("body"));
    }

    @Test
    public void updatesManyTimes() throws SQLException {
        storage.saveFeed("128.168-2/20\\30:807a", "body1");  
        storage.saveFeed("128.168-2/20\\30:807a", "body2");  
        storage.saveFeed("128.168-2/20\\30:807a", "body3");  
        storage.saveFeed("128.168-2/20\\30:807a", "body4");  
        storage.saveFeed("128.168-2/20\\30:807a", "body5");  
        storage.saveFeed("128.168-2/20\\30:807a", "body6");  
        storage.saveFeed("128.168-2/20\\30:807a", "body7");  
        storage.saveFeed("128.168-2/20\\30:807a", "body8");  
        storage.saveFeed("128.168-2/20\\30:807a", "body9");  
        storage.saveFeed("128.168-2/20\\30:807a", "body10");  
        storage.saveFeed("128.168-2/20\\30:807a", "body11");  
        storage.saveFeed("128.168-2/20\\30:807a", "body12");  
        storage.saveFeed("128.168-2/20\\30:807a", "body13");  
        storage.saveFeed("128.168-2/20\\30:807a", "body14");  
        storage.saveFeed("128.168-2/20\\30:807a", "body15");  
        storage.saveFeed("128.168-2/20\\30:807a", "body16");  
        storage.saveFeed("128.168-2/20\\30:807a", "body17");  
        storage.saveFeed("128.168-2/20\\30:807a", "body18");  
        storage.saveFeed("128.168-2/20\\30:807a", "body19");  
        storage.saveFeed("128.168-2/20\\30:807a", "body20");  
        storage.saveFeed("128.168-2/20\\30:807a", "body21");  
        storage.saveFeed("128.168-2/20\\30:807a", "body23");  
        storage.saveFeed("128.168-2/20\\30:807a", "body24");  
        storage.saveFeed("128.168-2/20\\30:807a", "body25");  

        ResultSet feeds = statement.executeQuery("SELECT * FROM feeds WHERE ip_port = '128.168-2/20\\30:807a'");
        
        assertEquals(true, feeds.next());
        assertEquals("body25", feeds.getString("body"));
    }

    @Test 
    public void saveManyUpdateOne() throws SQLException {
        storage.saveFeed("128.169.1.20:4000", "<?>?><<>??>}{|P}");
        storage.saveFeed("128.170.1.20:4000", "<?>?><<>??>}{|P}");
        storage.saveFeed("128.180.1.20:4000", "<?>?><<>??>}{|P}");
        storage.saveFeed("128.190.1.20:4000", "<?>?><<>??>}{|P}");
        storage.saveFeed("128.200.1.20:4000", "<?>?><<>??>}{|P}");

        storage.saveFeed("128.190.1.20:4000", "Updated Feed <?>\":{|}+_)(*&^%#@#$%^&*(");

        ResultSet feeds = statement.executeQuery("SELECT * FROM feeds WHERE ip_port = '128.190.1.20:4000'");
        assertEquals(true, feeds.next());
        assertEquals( "Updated Feed <?>\":{|}+_)(*&^%#@#$%^&*(", feeds.getString("body"));
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

    // @Test
    // public void deletesFiveOldFeed() throws SQLException, InterruptedException {
    //     storage.saveFeed("3:4:5::::6:8:5:8:f8c:8000", "test body");
    //     storage.saveFeed("/128.169.102.1", "test body");
    //     storage.saveFeed("id_string", "test body");
    //     storage.saveFeed("#@!id string is here", "test body");
    //     storage.saveFeed("ipipip***&&&string ^^_}{", "test body");
    //     storage.saveFeed("                        ", "test body");
    //     Thread.sleep(12*1000);
    //     storage.deleteOldFeeds();
    //     ResultSet resultSet = statement.executeQuery("select * from feeds");
    //     assertEquals(false, resultSet.next());
    // }

    // @Test
    // public void doesntDeleteFiveOldFeeds() throws SQLException, InterruptedException { 
    //     storage.saveFeed("3:4:5::::6:8:5:8:f8c:8000", "test body");
    //     storage.saveFeed("/128.169.102.1", "test body");
    //     storage.saveFeed("id_string", "test body");
    //     storage.saveFeed("#@!id string is here", "test body");
    //     storage.saveFeed("ipipip***&&&string ^^_}{", "test body");
    //     storage.saveFeed("                        ", "test body");
    //     Thread.sleep(11*1000);
    //     storage.deleteOldFeeds();
    //     ResultSet resultSet = statement.executeQuery("select * from feeds");
    //     for (int i = 0; i < 6; i++) {
    //         assertEquals(true, resultSet.next());
    //     }
    //     assertEquals(false, resultSet.next());
    // }

    // @Test
    // public void onlyDeletesOldFeeds() throws SQLException, InterruptedException {
    //     storage.saveFeed("3:4:5::::6:8:5:8:f8c:8000", "test body");
    //     storage.saveFeed("/128.169.102.1", "test body");
    //     storage.saveFeed("id_string", "test body");
    //     Thread.sleep(5*1000);
    //     storage.saveFeed("#@!id string is here", "test body");
    //     storage.saveFeed("ipipip***&&&string ^^_}{", "test body");
    //     storage.saveFeed("                        ", "test body");
    //     Thread.sleep(7*1000);
    //     storage.deleteOldFeeds();
    //     ResultSet resultSet = statement.executeQuery("select * from feeds");
    //     for (int i = 0; i < 3; i++) {
    //         assertEquals(true, resultSet.next());
    //     }
    //     assertEquals(false, resultSet.next());

    // }

    @After
    public void clearsTable() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("DELETE FROM feeds");

        connection.close();
    }

}
