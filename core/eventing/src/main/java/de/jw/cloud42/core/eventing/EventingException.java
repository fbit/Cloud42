package de.jw.cloud42.core.eventing;


/** 
 * Class to convey a exception that occured in eventing module.
 * 
 * @author fbitzer
 */
public class EventingException extends Exception {

    public EventingException(String cause) {
        super(cause);
    }

    public EventingException(String cause, Exception superException) {
        super(cause, superException);
    }

    public EventingException(Exception superException) {
        super(superException);
    }
}
