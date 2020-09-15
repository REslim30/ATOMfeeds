package atom;

import java.lang.Exception;
/**
 * InvalidAtomException
 */
public class InvalidAtomException extends Exception {
    InvalidAtomException(String s) {
        super("RFC-4287: " + s);
    }
}
