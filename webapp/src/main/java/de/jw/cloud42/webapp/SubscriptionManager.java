/**
 * 
 */
package de.jw.cloud42.webapp;


import java.util.List;

import java.util.logging.Logger;



import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.faces.FacesMessages;


import de.jw.cloud42.core.eventing.storage.DefaultSubscriberStore;
import de.jw.cloud42.core.eventing.subscription.SOAPSubscription;
import de.jw.cloud42.core.eventing.subscriptionProcessor.GenericSubscriptionProcessor;

/**
 * Seam component for handling subscriptions.
 * Caution: handles only <code>SOAPSubscriptions</code>, 
 * no generic subscriptions like the Web Service application.
 * 
 * @author fbitzer
 * 
 */
@Name("subscriptionManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout=1000000000)
public class SubscriptionManager {

		
	/**
	 * Inject faces messages to trigger error and success messages.
	 */
	@In 
	FacesMessages facesMessages;

	/**
	 * injected Hibernate session
	 */
	@In 
	Session session;
	
	/**
	 * The list of subscriptions.
	 */
	private List<SOAPSubscription> subscriptionList = null;
	
	
	
	/**
	 * @return the subscriptionList
	 */
	public List<SOAPSubscription> getSubscriptionList() {
		
		//load form DB if list not in cache
		if (this.subscriptionList == null){
			
			session.beginTransaction();
			//Note: only real SOAPSubscriptions are loaded!
			Query q = session.createQuery("SELECT s FROM SOAPSubscription s");
			session.getTransaction().commit();
			
			subscriptionList = q.list();

			
		}
		
		
		return subscriptionList;
		
	}



	/**
	 * Reset subscriptionList and force reload next time it is accessed.
	 */
	public void resetSubscriptionList(){
	
		this.subscriptionList = null;
	}
	
	
	/**
	 * Creates a new SOAPSubscription and stores it in database.
	 * 
	 * @param topic
	 * @param endpointAddress
	 */
	public void subscribe(String topic, String endpointAddress){
		
		
		SOAPSubscription s = new SOAPSubscription();
		
		s.setTopic(topic);
		
		try {
			s.setId(GenericSubscriptionProcessor.generateId());
			
			EndpointReference epr =  new EndpointReference(endpointAddress);
			
			s.setToEndpoint(epr);
			
		} catch (Exception ex){
			Logger.getAnonymousLogger().severe("Error creating a subsription: " + ex.getMessage());
		}
		
		
		DefaultSubscriberStore store = new DefaultSubscriberStore();
		
		store.store(s);
		
		this.resetSubscriptionList();
		
		
	}
	/**
	 * Remove a subscription identified by its id.
	 * 
	 * @param subscriptionId
	 */
	public void unsubscribe(String subscriptionId){
		DefaultSubscriberStore store = new DefaultSubscriberStore();
		
		store.delete(subscriptionId);
		
		this.resetSubscriptionList();
		
	}
	
	
}
