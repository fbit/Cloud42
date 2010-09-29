
package de.jw.cloud42.core.eventing.storage;


import java.util.Iterator;
import java.util.List;



import org.hibernate.Query;
import org.hibernate.Session;

import de.jw.cloud42.core.eventing.subscription.Subscription;
import de.jw.cloud42.core.hibernate.HibernateUtil;


/**
 * Default implementation of a subscriber store.
 * Uses a database with Hibernate to save Subscriptions.
 * 
 * 
 * @author fbitzer
 *
 */
public class DefaultSubscriberStore implements SubscriberStore {

	
	
    /**
	* Get a subscriber.
	* @return The Subscription or null if no subscription with given id exists.
	*/
    public Subscription retrieve(String id) {
       
    	
    	Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        session.beginTransaction();

        Subscription s;
		try {
			s = (Subscription) session.load(Subscription.class, id);
			  
		} catch (Exception ex) {
			s = null;
		}
          
	    session.getTransaction().commit();
	    
	    return s;
	    
   
    }
   

    /**
     * Save a subscriber.
     * 
     * @param s the Subscription to store.
     * @param topic the topic for the subscription.
     */
    public void store(Subscription s) {
    	
    	Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        session.beginTransaction();

        session.save(s);
    	
        session.getTransaction().commit();
 	   
    	
        
    }

    /**
     * Delete a subscriber.
     */
    public void delete(String subscriberID) {
    	Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        session.beginTransaction();
        
		try {
		   Object s = session.load(Subscription.class, subscriberID);
		   session.delete(s);
		  	  
		} catch (Exception ex) {
			
		}
        session.getTransaction().commit();
 	   
    }

    /**
     * Get an iterator over all present subscribers for a specific topic.
     * 
     * @param The topic.
     */
    public Iterator retrieveAllSubscribers(String topic) {

    	
    	Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        session.beginTransaction();

        Query q = session.createQuery("SELECT s FROM Subscription s WHERE s.topic = :topic");
	    q.setParameter("topic", topic);

	    List<Subscription> resultset = q.list();
	    
        session.getTransaction().commit();
        
        return resultset.iterator();


        
    }

}
