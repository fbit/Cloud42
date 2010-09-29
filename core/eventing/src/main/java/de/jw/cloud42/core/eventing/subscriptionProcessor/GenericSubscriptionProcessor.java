/**
 * 
 */
package de.jw.cloud42.core.eventing.subscriptionProcessor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.soap.SOAPBody;

import de.jw.cloud42.core.eventing.EventingException;
import de.jw.cloud42.core.eventing.storage.DefaultSubscriberStore;
import de.jw.cloud42.core.eventing.storage.SubscriberStore;
import de.jw.cloud42.core.eventing.subscription.Subscription;

/**
 * A generic implementation of a subscription processor.
 * 
 * Getting the subscriber form the subscription message is delegated to more
 * specific implementations.
 * 
 * @author fbitzer
 * 
 */
public class GenericSubscriptionProcessor implements SubscriptionProcessor {

	/**
	 * Processes a subscription request message.
	 * 
	 * @param subscriptionMessage
	 * @return Id of subscription, if processed successfully.
	 * @throws EventingException
	 */
	public String subscribe(OMElement subscriptionMessage, String topic)
			throws EventingException {
		SubscriberStore store = new DefaultSubscriberStore();

		if (store == null)
			throw new EventingException("Subscription store not found");

		Subscription subscription = getSubscriberFromMessage(subscriptionMessage);

		//set ID for subsccription
		subscription.setId(generateId());
		
		//set topic
		subscription.setTopic(topic);
		
		//and store the subscription
		store.store(subscription);

		return subscription.getId();
	}

	/**
	 * Processes a unsubscribe request message.
	 * 
	 * @param subscriptionId
	 *            id of the subscription to cancel.
	 */
	public void unsubscribe(String subscriptionId) throws EventingException {

		SubscriberStore store = new DefaultSubscriberStore();

		if (store == null)
			throw new EventingException("Subscription store not found");

		store.delete(subscriptionId);

		Logger.getAnonymousLogger()
				.info(
						"Subscription with id " + subscriptionId
								+ " was unsubscribed.");

	}

	/**
	 * Read subscriber informations from subscription message. Inherited classes
	 * must implement this method by themselves.
	 * 
	 * @param body
	 *            the SOAP body of the subscription message.
	 * @return
	 * @throws EventingException
	 */
	public Subscription getSubscriberFromMessage(OMElement message)
			throws EventingException {
		throw new UnsupportedOperationException(
				"Method not supported in generic subscription processor.");
	}

	/**
	 * Generates an Id for a subscription.
	 * 
	 * @return the generated Id as String
	 */
	public static String generateId() throws EventingException {
		// generate an ID for this subscription
		String id = UUIDGenerator.getUUID();

		try {
			URI uri = new URI(id);
			return uri.getSchemeSpecificPart();
		} catch (URISyntaxException e) {
			throw new EventingException(e);
		}
		
	}

}
