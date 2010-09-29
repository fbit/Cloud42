/**
 * 
 */
package de.jw.cloud42.webservice.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import de.jw.cloud42.core.endpoint.Cloud42Endpoint;
import de.jw.cloud42.core.eventing.storage.DefaultSubscriberStore;
import de.jw.cloud42.core.hibernate.HibernateUtil;

/**
 * Listener to start endpoint of Cloud42 for incoming notifications during servlet initialization.
 * 
 * @author fbitzer
 * 
 */

public final class StartupListener implements ServletContextListener {
	public void contextInitialized(ServletContextEvent event) {

		/*
		 * This method is called when the servlet context is initialized(when
		 * the Web Application is deployed). You can initialize servlet context
		 * related data here.
		 */

		//Start endpoint
		
		Cloud42Endpoint.getInstance().startEndpoint();
		
		
		

	}

	 public void contextDestroyed(ServletContextEvent event) {

	      /* This method is invoked when the Servlet Context 
	         (the Web Application) is undeployed or 
	         WebLogic Server shuts down.
	      */			    

	    }

}
