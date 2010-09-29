/**
 * 
 */
package de.jw.cloud42.core.eventing.factory;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.jw.cloud42.core.eventing.subscriptionProcessor.SubscriptionProcessor;

/**
 * Factory class that creates an instance of a concrete SubscriptionProcessor class.
 * Uses Factory pattern.
 * 
 * @author fbitzer
 *
 */
public class SubscriptionProcessorFactory {
	
	/**
	 * Create a SubscriptionProcessor class.
	 * @param className the fully qualified classname of the desired subscription processor.
	 * @return
	 */
	public SubscriptionProcessor createSubscriptionProcessor(String className){
		
		Class cls = null;;
		
		try {
			 cls = Class.forName(className);
		} catch (ClassNotFoundException cex){
			Logger.getAnonymousLogger().log(Level.SEVERE, "Subscription procossor class " + className + " not found.");
		}
		
		Object obj = null;
		try {
			obj = cls.newInstance();
		} catch (InstantiationException iex){
			Logger.getAnonymousLogger().log(Level.SEVERE, "Error instantiating subscription processor class " + className + ".");
		}
		 catch (IllegalAccessException iaex){
			Logger.getAnonymousLogger().log(Level.SEVERE, "Error instantiating subscription processor class " + className + ".");
		}
		 catch (Exception ex){
				Logger.getAnonymousLogger().log(Level.SEVERE, "Error instantiating subscription processor class " + className + ". Class not found.");
		 }
		
		
		 if (obj instanceof SubscriptionProcessor){
			 return (SubscriptionProcessor)obj;
		 } else {
			 return null;
		 }
	}
		
}

