package de.jw.cloud42.core.endpoint;

import java.io.StringReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;

import javax.xml.bind.JAXBContext;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import javax.xml.transform.Source;

import javax.xml.transform.stream.StreamSource;

import javax.xml.ws.BindingType;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;


import org.hibernate.Query;
import org.hibernate.Session;

import de.jw.cloud42.core.domain.Settings;
import de.jw.cloud42.core.eventing.Message;
import de.jw.cloud42.core.eventing.notification.NotificationManager;
import de.jw.cloud42.core.eventing.storage.DefaultSubscriberStore;
import de.jw.cloud42.core.eventing.storage.SubscriberStore;
import de.jw.cloud42.core.eventing.subscription.Subscription;
import de.jw.cloud42.core.hibernate.HibernateUtil;

/**
 * 
 * This is an endpoint with a HTTP binding to receive POST notification messages
 * sent from an AMI instance to Cloud42.
 * Per default, it listens to http://localhost:8084/messages, but the address can be changed using the
 * Web Service interface of Cloud42.
 * 
 * 
 * It uses JAX-WS for a RESTful web service implementation.
 * 
 * Incoming messages are parsed into
 * <code>de.jw.cloud42.core.eventing.Message</code> objects using JAXB.
 * 
 * <b>Important:</b> Note that the HTTP content type explicitly must be set to text/xml so that a message
 * can be parsed correctly.
 * 
 * @author fbitzer
 */
@WebServiceProvider
@BindingType(value = HTTPBinding.HTTP_BINDING)
@ServiceMode(value = Service.Mode.MESSAGE)
public class Cloud42Endpoint implements Provider<Source> {

	/**
	 * The default address of the Cloud42 endpoint if no settings are specified.
	 */
	private final String DEFAULT_ENDPOINT_ADDRESS = "http://localhost:8084/messages";
	
	
	//use Singleton pattern
	private static Cloud42Endpoint theInstance;
	
	@Resource
	protected WebServiceContext wsContext;

	private JAXBContext jc;

	
	//the current endpoint
	Endpoint endpoint = null;
	
	/**
	 * Private Constructor; initialize JAXB context
	 */
	private Cloud42Endpoint() {
		try {
			jc = JAXBContext.newInstance(Message.class);

		} catch (JAXBException je) {
			throw new WebServiceException("Cannot create JAXBContext", je);
		}
	}
	
	/**
	 * Singleton method.
	 * @return current instance.
	 */
	public static Cloud42Endpoint getInstance(){
		if (theInstance == null){
			theInstance = new Cloud42Endpoint();
		}
		
		return theInstance;
	}

	/**
	 * Invoke method of web service. Process incoming message.
	 */
	public Source invoke(Source request) {

		MessageContext mc = wsContext.getMessageContext();

		String httpMethod = (String) mc.get(MessageContext.HTTP_REQUEST_METHOD);

		// Only POST messages are processed
		if (httpMethod.equals("POST")) {

			return post(request, mc);

		} else {

			mc.put(MessageContext.HTTP_RESPONSE_CODE, 400);
			return null;
		}

	}

	/**
	 * Create Cloud42 endpoint with HTTP Binding. Address is read from settings.
	 * If an endpoint is running, it is closed first.
	 * 
	 * @param args
	 */
	public void startEndpoint() {
		try {

			
			//if there is a running endpoint, close it
			if (endpoint != null){
				endpoint.stop();
				
				Logger.getAnonymousLogger().info("Currently running Cloud42 message endpoint was closed.");
				
			}
			
			endpoint = Endpoint.create(HTTPBinding.HTTP_BINDING,
					new Cloud42Endpoint());
			
			//read the address for the new endpoint
			String address = getAddress();
			
			endpoint.publish(address);

			Logger.getAnonymousLogger().info("------------------------------------------------");
			Logger.getAnonymousLogger().info("Cloud42 message endpoint published at " + address);
			Logger.getAnonymousLogger().info("------------------------------------------------");
			
		} catch (Exception ex) {
			Logger.getAnonymousLogger().log(Level.SEVERE,
					"Creating Cloud42 endpoint failed: " + ex.getMessage());
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * Reads the address of the Cloud42 endpoint from the configuration entry in the database.
	 * If no settings entry exists, a default value is returned and saved to database.
	 * 
	 * @return Address of Cloud42 endpoint for incoming notifications.
	 */
	public String getAddress(){
		
		String address = Settings.getInstance().getEndpointAddress();
		
		//no address, so set default
		if (address == null) {
			address = DEFAULT_ENDPOINT_ADDRESS;
			Settings.getInstance().setEndpointAddress(address);
			Settings.getInstance().save();
		}
		
		return address;
		

	}
	/**
	 * Set address of Cloud42 endpoint.
	 * Causes restart of endpoint with new address.
	 * 
	 * @param newAddress the address to set.
	 */
	public void setAddress(String newAddress){
		
		Settings.getInstance().setEndpointAddress(newAddress);
		Settings.getInstance().save();
		
		//restart endpoint
		this.startEndpoint();
	}
	
	
	/**
	 * Handles HTTP POST and starts notifiying subscribers.
	 */
	private Source post(Source source, MessageContext mc) {

		String replyElement;
		try {
			// parse message
			Unmarshaller u = jc.createUnmarshaller();
			Message message = (Message) u.unmarshal(source);

			Logger.getAnonymousLogger().info(
					"Cloud42 endpoint retrieved a event message. InstanceId is: "
							+ message.instanceId);

			// set Http Status Code 202 for "Accepted"
			mc.put(MessageContext.HTTP_RESPONSE_CODE, 202);
			replyElement = "<reply>OK</reply>";

			
			// Notify subscribers
			NotificationManager.notifySubscribers(message);

		} catch (Exception ex) {
			// error parsing the request, so set status code 400 (Bad Request)
			Logger.getAnonymousLogger().log(Level.WARNING,
					"Error parsing incoming event message: " + ex.getMessage() 
					+ ". Maybe HTTP content-type was wrong (must be text/xml) or invalid message format.");

			mc.put(MessageContext.HTTP_RESPONSE_CODE, 400);
			replyElement = "<reply>" + ex.getMessage() + "</reply>";

		}

		StreamSource reply = new StreamSource(new StringReader(replyElement));
		return reply;

	}

}
