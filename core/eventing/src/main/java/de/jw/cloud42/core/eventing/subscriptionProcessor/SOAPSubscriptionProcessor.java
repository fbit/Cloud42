/**
 * 
 */
package de.jw.cloud42.core.eventing.subscriptionProcessor;

import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;



import de.jw.cloud42.core.eventing.EventingConstants;
import de.jw.cloud42.core.eventing.EventingException;
import de.jw.cloud42.core.eventing.subscription.SOAPSubscription;
import de.jw.cloud42.core.eventing.subscription.Subscription;

/**
 * SubscriptionProcessor class for a subscriber that receives notifications as SOAP messages over HTTP.
 * 
 * @author fbitzer
 * 
 */
public class SOAPSubscriptionProcessor extends GenericSubscriptionProcessor {

	/**
	 * Parses the subscription message and identifies the endpoint to send notifications to.
	 * Endpoint information is given according to WS-Eventing specification.
	 * 
	 */
	@Override
	public Subscription getSubscriberFromMessage(OMElement message) throws EventingException {

		SOAPSubscription subscriber = new SOAPSubscription();
		
		
		
		//parse message
				
		OMElement subscribeElement = message.getFirstChildWithName(new QName(
				EventingConstants.EVENTING_NAMESPACE,
				EventingConstants.ElementNames.Subscribe));
		if (subscribeElement == null)
			throw new EventingException("'Subscribe' element is not present");

		OMElement deliveryElement = subscribeElement
				.getFirstChildWithName(new QName(
						EventingConstants.EVENTING_NAMESPACE,
						EventingConstants.ElementNames.Delivery));
		if (deliveryElement == null)
			throw new EventingException("Delivery element is not present");

		OMElement notifyToElement = deliveryElement
				.getFirstChildWithName(new QName(
						EventingConstants.EVENTING_NAMESPACE,
						EventingConstants.ElementNames.NotifyTo));
		if (notifyToElement == null)
			throw new EventingException("NotifyTo element is null");

		EndpointReference notifyToEPr = null;

		try {
			notifyToEPr = EndpointReferenceHelper.fromOM(notifyToElement);
		} catch (AxisFault af) {
			throw new EventingException(af);
		}

			
		subscriber.setToEndpoint(notifyToEPr);
		
		
        
        Logger.getAnonymousLogger().info("Subscription received. Subscribed Endpoint: "
				+ notifyToEPr.getAddress());
	
		
		return subscriber;
	}

}
