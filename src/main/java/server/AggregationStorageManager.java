package main.java.server;

import java.nio.file.*;
import java.io.*;

/**
 * AggregationStorageManager
 */
public class AggregationStorageManager {
    //where server places all the files.
    //Assumes java is being called within the
    //topmost parent directory
    private final static Path serverResources = new File("src/main/resources/server").toPath();

    public static void save(long lamportClock, long connectionId, String body) throws IOException {
        String fileName = Long.toString(lamportClock) + "_" + Long.toString(connectionId) + ".xml";
        Files.write(Paths.get(serverResources.toString(), "/", fileName), body.getBytes(), StandardOpenOption.CREATE_NEW); 
    }

    public static void retrieve(long lamportClock, long connectionId) throws IOException {
        
    }
}
