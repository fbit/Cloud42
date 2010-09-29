/**
 * 
 */
package de.jw.cloud42.core.eventing.notification;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.jw.cloud42.core.eventing.Message;
import de.jw.cloud42.core.eventing.storage.DefaultSubscriberStore;
import de.jw.cloud42.core.eventing.storage.SubscriberStore;
import de.jw.cloud42.core.eventing.subscription.Subscription;

/**
 * This class provides a static method to start the notification process.
 * 
 * @author fbitzer
 *
 */
public class NotificationManager {

	/**
	 * Notifies all stored subscribers.
	 * 
	 * @param message the notification message. Each subscriber transforms this message to its own format (e.g. a
	 * SOAP message) before sending.
	 */
	public static void notifySubscribers(Message message){
		
		//iterate over subscribers and call sendEventData on each subscriber
		SubscriberStore store = new DefaultSubscriberStore();
		try {
			
			
			for (Iterator iter = store.retrieveAllSubscribers(message.topic); iter.hasNext();) {
				Subscription s = (Subscription) iter.next();
	         	
				s.sendEventData(message);
				
				//this causes the current thread to sleep 750 ms so that messages do not overlap
				Thread.sleep(750);
			}
		
		} catch (Exception ex){
			Logger.getAnonymousLogger().log(Level.SEVERE, "Error notifiying all subscribers: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
}
