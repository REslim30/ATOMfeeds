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

    //Saves feed in a file of the following format:
    //<lamportClock>_<connectionId>.xml
    public static synchronized void save(long lamportClock, long connectionId, String body) throws IOException {
        String fileName = Long.toString(lamportClock) + "_" + Long.toString(connectionId) + ".xml";
        Files.write(Paths.get(serverResources.toString(), "/", fileName), body.getBytes(), StandardOpenOption.CREATE_NEW); 
    }

    //Aggregrates all feeds that were saved before caller (in terms of lamport clocks)
    //Returns feeds in a single String delimited by \r\n
    //Assumes files are formatted: <lamportClock>_<connectionId>.xml
    public static synchronized String retrieve(long lamportClock, long connectionId) throws IOException {
        File serverResourcesDir = serverResources.toFile();
        StringBuilder bodyBuilder = new StringBuilder();

        for (File file: serverResourcesDir.listFiles()) {
            //Gets lamport clock values
            String fileName = file.getName();
            int delim = fileName.indexOf('_');
            long fileLamportClock = Long.parseLong(fileName.substring(0,delim));
            long fileConnectionId = Long.parseLong(fileName.substring(delim+1, fileName.indexOf('.')));

            if (lamportClock < fileLamportClock || (lamportClock == fileLamportClock && connectionId < fileConnectionId))
                continue;

            if (!(bodyBuilder.length() == 0)) 
                bodyBuilder.append("\r\n");

            bodyBuilder.append(Files.readString(file.toPath()));
        }
        return bodyBuilder.toString();
    }
}
