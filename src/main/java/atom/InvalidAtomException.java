package atom;

import java.lang.Exception;
/**
 * InvalidAtomException
 * Exception thrown when invalid atom element 
 * according to RFC-4287
 */
public class InvalidAtomException extends Exception {
    InvalidAtomException(String s) {
        super("RFC-4287: " + s);
    }
}
