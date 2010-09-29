/**
 * 
 */
package de.jw.cloud42.core.eventing.subscriptionProcessor;



import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;


import de.jw.cloud42.core.eventing.EventingException;


/**
 * A subscription processor is a class that handles the subscription process for a particular kind of
 * subscribing endpoint (e.g. a SOAP/HTTP endpoint).
 * 
 * Concrete implementations of this interface extract endpoint information out of the subscription message
 * that is contained in the SOAPBody.
 * 
 * @author fbitzer
 *
 */
public interface SubscriptionProcessor {
	
	/**
	 * Processes a subscription request message.
	 * 
	 * @param subscriptionMessage
	 * @return Id of subscription, if processed successfully.
	 * @throws EventingException
	 */
	public String subscribe(OMElement subscriptionMessage, String topic) throws EventingException;
	
	/**
	 * Handles unsubscribing.
	 * @param subscriptionId the id of the subscription to cancel.
	 * @throws EventingException
	 */
	public void unsubscribe(String subscriptionId) throws EventingException;

	
	
}
