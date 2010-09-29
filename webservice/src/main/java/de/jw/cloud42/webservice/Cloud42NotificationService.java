/**
 * 
 */
package de.jw.cloud42.webservice;


import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;




import de.jw.cloud42.core.endpoint.Cloud42Endpoint;
import de.jw.cloud42.core.eventing.factory.SubscriptionProcessorFactory;
import de.jw.cloud42.core.eventing.subscriptionProcessor.GenericSubscriptionProcessor;
import de.jw.cloud42.core.eventing.subscriptionProcessor.SubscriptionProcessor;



/**
 * Web service class for Cloud42 notification service. Provides methods to subscribe and unsubscribe to notification
 * messages coming from AMI instances. Subscription/Unsubscription requests are send as SOAP messages.
 * 
 * @author fbitzer
 *
 */
public class Cloud42NotificationService {
	
	/**
	 * Subscribe to notification messages.
	 *
	 * @param subscriptionProcessor a fully qualified name of the class that handles the subscription request.
	 * This class is responsible for parsing the subscription message, to extract endpoint informations out of the
	 * subscriptionMessage part etc.
	 * @param topic the topic the subscriber wants to subscribe to.
	 * @param subscriptionMessage This part of the SOAP message contains endpoint information and is parsed by
	 * the subscriptionProcessor.
	 * 
	 * @return a UUID to identify the subscription.
	 * @throws Exception
	 */
	public String subscribe(String subscriptionProcessor, String topic, OMElement subscriptionMessage) throws Exception{
		
		
		SubscriptionProcessorFactory f = new SubscriptionProcessorFactory();
		
		SubscriptionProcessor p = f.createSubscriptionProcessor(subscriptionProcessor);
		
		MessageContext msgContext = MessageContext.getCurrentMessageContext();
	
		
		return p.subscribe(subscriptionMessage, topic);
	}
	/**
	 * Unsubscribe.
	 * @param subscriptionId UUID of subscription to cancel.
	 * 
	 * @return true if successful (return value is necessary because of Exception handling)
	 * 
	 * @throws Exception
	 * 
	 */
	public boolean unsubscribe(String subscriptionId) throws Exception {
		
		SubscriptionProcessor p = new GenericSubscriptionProcessor();
		
		p.unsubscribe(subscriptionId);
	
		return true;
	}

	/**
	 * Get current address of Cloud42 message endpoint.
	 * @return the address of the Cloud42 message endpoint used for sending notifications from
	 * AMI instances to Cloud42.
	 */
	public String getEndpointAddress(){
		return Cloud42Endpoint.getInstance().getAddress();
	}
	
	/**
	 * Set address of Cloud42 message endpoint (the endpoint published by Cloud42 for retrieving and dsipatching
	 * event messages from AMI instances) and cause its restart.
	 * 
	 * @param address HTTP Endpoint address such as <code>http://localhost:8084/messages</code>
	 */
	public void setEndpointAddress(String address){
		Cloud42Endpoint.getInstance().setAddress(address);
	}
}
