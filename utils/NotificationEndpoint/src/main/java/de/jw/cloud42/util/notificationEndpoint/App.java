package de.jw.cloud42.util.notificationEndpoint;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataSource;


import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.http.HTTPBinding;

/**
 * 
 * This is a simple HTTP endpoint on http://localhost:8085/monitor that prints out received messages.
 * It uses JAX-WS for implementing Web Service capabilities on message level.
 * 
 * @author fbitzer
 */
@WebServiceProvider
@ServiceMode(value = Service.Mode.MESSAGE)
public class App implements Provider<DataSource> {

	public App() {
	}

	/**
	 * Invoke method of web service. Process incoming message.
	 */
	public DataSource invoke(DataSource request) {

		try {
			//simply print out the received message
			Logger.getAnonymousLogger().info("Message retreived. Message is: "
					+ streamToString(request.getInputStream()));

		} catch (Exception ex) {

			Logger.getAnonymousLogger().log(Level.SEVERE,
					"Error parsing message.");
			ex.printStackTrace();
		}

		return null;
		
	}


	
	/**
	 * Main Method of NotificationEndpoint. Create an endpoint with HTTP Binding for monitoring messages.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		String address = "http://localhost:8085/monitor";
		
		if (args.length > 0){
			address = args[0];
		}
		
		Endpoint e = Endpoint.create(HTTPBinding.HTTP_BINDING, new App());
		e.publish(address);

		System.out.println("--------------------------------------------------");
		System.out.println("Monitoring HTTP endpoint published at " + address);
		System.out.println("Waiting for incoming messages...");
		System.out.println("--------------------------------------------------");
		// Run forever e.stop();
	}
	
	/**
	 * Convert an InputStream to String.
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private String streamToString(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}
}
