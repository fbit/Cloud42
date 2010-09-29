/**
 * 
 */
package de.jw.cloud42.core.eventing;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is the internal representation of a notification message that can be sent from an EC2 instance
 * to the Cloud42 endpoint.
 * 
 * Note: Due to the fact that the message is parsed by JAXB it simply consists of public fields 
 * instead of properties with getters and setters.
 * 
 * @author fbitzer
 * 
 */
@XmlRootElement(namespace="http://cloud42.jw.de/message")
public class Message {

	
	/**
	 * Topic of the message.
	 */
	@XmlElement(namespace="http://cloud42.jw.de/message", required=true)
	public String topic;
	
	/**
	 * InstanceId of the instance that fired the message.
	 */
	@XmlElement(namespace="http://cloud42.jw.de/message")
	public String instanceId;
	
	/**
	 * Date and time when the message was sent (String field for compability reasons).
	 */
	@XmlElement(namespace="http://cloud42.jw.de/message")
	public String timestamp;
	
	/**
	 * The text of the message.
	 */
	@XmlElement(namespace="http://cloud42.jw.de/message")
	public String text;
	
	/**
	 * Space of additional informations if required.
	 */
	@XmlElement(namespace="http://cloud42.jw.de/message")
	public String info;
	


	
}
