
package de.jw.cloud42.core.eventing;
/**
 * 
 * Some constants for usage within the eventing subproject.
 * 
 * @author fbitzer
 *
 */
public interface EventingConstants {

    String EVENTING_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/08/eventing";
    
    String NOTIFICATION_NAMESPACE = "http://webservice.cloud42.jw.de/notification";
    
    interface ElementNames {
        String Subscribe = "Subscribe";
        String Delivery = "Delivery";
        String NotifyTo = "NotifyTo";
    }

}
