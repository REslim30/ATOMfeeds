package server;

/**
 * LamportClock
 * 
 * A custom counter with atomic operations.
 */
public class LamportClock {
      private long value = 0;  
      public LamportClock(long value) {
          this.value = value;
      }

      //Increments internal value and returns
      public synchronized long incrementAndGet() {
          return ++value;
      }


      //Sets the max of input and increments by one
      public synchronized void setMaxAndIncrement(long otherValue) {
          value = Long.max(value, otherValue);
          value++;
      }
}
