

package de.jw.cloud42.core.eventing.storage;



import java.util.Iterator;

import de.jw.cloud42.core.eventing.EventingException;
import de.jw.cloud42.core.eventing.subscription.Subscription;

/** Defines the Storage for storing subscribers. 
 * */
public interface SubscriberStore {

   
    /**
     * To store the subscriber.
     *
     * @param s
     * @throws EventingException
     */
    void store(Subscription s) throws EventingException;

    /**
     * To retrieve a previously stored subscriber.
     *
     * @param subscriberID
     * @return
     * @throws EventingException
     */
    Subscription retrieve(String subscriberID) throws EventingException;

    /**
     * To retrieve all subscribers stored upto now.
     *
     * @return
     * @throws EventingException
     */
    Iterator retrieveAllSubscribers(String topic) throws EventingException;

    
    /**
     * To delete a previously stored subscriber.
     *
     * @param subscriberID
     * @throws EventingException
     */
    void delete(String subscriberID) throws EventingException;

    	
}
