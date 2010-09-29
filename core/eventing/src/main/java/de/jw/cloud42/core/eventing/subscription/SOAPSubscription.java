/**
 * 
 */
package de.jw.cloud42.core.eventing.subscription;

import java.net.URI;
import java.util.logging.Logger;

import javax.persistence.Entity;
import javax.persistence.Id;




import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.http.HTTPConstants;

import de.jw.cloud42.core.eventing.EventingConstants;
import de.jw.cloud42.core.eventing.EventingException;
import de.jw.cloud42.core.eventing.Message;

/**
 * This is an implementation of a subscription for an endpoint that receives notification messages 
 * as SOAP messages over HTTP.
 * 
 * @author fbitzer
 *
 */
@Entity
public class SOAPSubscription extends Subscription {

	 
	 /**
	  * Endpoint where notifications must be send to.
	  */
	 private EndpointReference toEndpoint;
	 
	

	/**
	 * Transforms the provided notification message into a SOAP message and sends it.
	 * 
	 * @see de.jw.cloud42.core.eventing.subscription.Subscription#sendEventData(Message message)
	 * @param message the message to send as SOAP message.
	 */
	@Override
	public void sendEventData(Message message) throws Exception {
		//send a SOAP message to the subscribing endpoint
		
		//create an OMElement out of the message
		OMFactory factory = OMAbstractFactory.getOMFactory();
		
		OMNamespace ns = factory.createOMNamespace(EventingConstants.NOTIFICATION_NAMESPACE, "msg");

		OMElement messageElement = factory.createOMElement("message", ns);
		
		
		OMElement topicElement = factory.createOMElement("topic", ns);
		messageElement.addChild(topicElement);
		topicElement.setText(message.topic);
		
		OMElement idElement = factory.createOMElement("instanceId", ns);
		messageElement.addChild(idElement);
		idElement.setText(message.instanceId );
		
		
		
		OMElement timeElement = factory.createOMElement("timestamp", ns);
		messageElement.addChild(timeElement);
		timeElement.setText(message.timestamp);
		
		OMElement textElement = factory.createOMElement("text", ns);
		messageElement.addChild(textElement);
		textElement.setText(message.text);
		
		OMElement infoElement = factory.createOMElement("info", ns);
		messageElement.addChild(infoElement);
		infoElement.setText(message.info);


		sendThePublication(messageElement);
		
		
	}

	/**
	 * @return the toEndpoint
	 */
	public EndpointReference getToEndpoint() {
		return toEndpoint;
	}

	/**
	 * @param toEndpoint the toEndpoint to set
	 */
	public void setToEndpoint(EndpointReference toEndpoint) {
		this.toEndpoint = toEndpoint;
	}

	
	/**
	 * Send data to subscriber's endpoint using Axis2 ServiceClient.
	 * 
	 * @param eventData OMElement containing the data to put on the wire.
	 * @throws EventingException
	 */
	private void sendThePublication(OMElement eventData) throws EventingException {

		Logger.getAnonymousLogger().info("Sending notification to " + this.getToEndpoint().getAddress());
		
        EndpointReference deliveryEPR = this.getToEndpoint();
        try {
            ServiceClient sc = new ServiceClient();
            Options options = new Options();
            
            options.setTo(deliveryEPR);
            
            options.setProperty(MessageContext.TRANSPORT_NON_BLOCKING, Boolean.FALSE);
            
            options.setProperty(Constants.Configuration.CONTENT_TYPE, HTTPConstants.MEDIA_TYPE_APPLICATION_SOAP_XML);

            sc.setOptions(options);
            
            sc.fireAndForget(eventData);
                
        } catch (AxisFault e) {
            throw new EventingException(e);
        
		 } catch (Exception ex) {
	         throw new EventingException(ex);
	     }
    }
	
}
