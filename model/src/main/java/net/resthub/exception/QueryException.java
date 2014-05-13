package net.resthub.exception;

/**
 * QueryException
 * @author valdo
 */
public class QueryException extends RuntimeException {

    public QueryException(Exception ex) {
        super(ex.getMessage(), ex);
    }
    
    public QueryException(String message) {
        super(message);
    }

    public QueryException(String message, Object... args) {
        super(String.format(message, args));
    }
    
}
