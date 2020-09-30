package server;

import server.LamportClock;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.*;

/**
 * LamportClockTest
 *
 * Tests to ensure the LamportClock class is thread safe.
 */
public class LamportClockTest {
    @Test
    public void canIncrementAndGet() throws InterruptedException {
        LamportClock lamportClock = new LamportClock(0);

        ArrayList<Thread> threads = new ArrayList<Thread>();

        //Create 1000 threads that increment lamportClock 2000 times
        for (int i = 0; i < 1000; i++) {
            threads.add(new Thread() {
                @Override
                public void run() {
                    for (int j = 0; j < 2000; j++) {
                        lamportClock.incrementAndGet();
                    }
                }
            });
        }

        //Start the threads
        for (Thread x : threads) {
            x.start();
        }

        //Wait for all threads to finish
        for (Thread x : threads) {
            x.join();
        }

        assertEquals(lamportClock.incrementAndGet(), 2000001);
    }

    @Test
    public void canSetMaxAndIncrement() throws InterruptedException {
        LamportClock lamportClock = new LamportClock(0);

        ArrayList<Thread> threads = new ArrayList<Thread>();

        Random rand = new Random();
        long maxValue = 0;
        //Create 1000 threads that are each given a random number
        //That tries to setMax
        for (int i = 0; i < 1000; i++) {
            long curValue = rand.nextLong();

            maxValue = Long.max(curValue, maxValue);

            threads.add(new Thread() {
                private long value = curValue;

                @Override
                public void run() {
                    lamportClock.setMaxAndIncrement(value);
                }
            });
        }

        //Start the threads
        for (Thread x : threads) {
            x.start();
        }

        //Wait for all threads to finish
        for (Thread x : threads) {
            x.join();
        }

        assertEquals(true, lamportClock.incrementAndGet() > maxValue + 1);
    }
}
