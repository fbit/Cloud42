/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.jw.cloud42.core.eventing.subscription;


import java.net.URI;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import de.jw.cloud42.core.eventing.Message;

/**
 * A Subscription entity saves information belonging to a subscribing endpoint, such as its subscription id.
 * 
 * The method <code>sendEventData</code> is used to send notification messages. The concrete format and transport
 * protocol are definded in concrete implementations of this class.
 */

@Entity
public abstract class Subscription {
	
	/**
	 * Id of subscriber/subscription
	 */
	 private String id;
	 
	 /**
	  * Topic for this Subscription
	  */
	 private String topic;
	 
	 
	/**
	 * 
	 * @return the subscription id.
	 */
	@Id
	public String getId() {
		
		return id;
	}

	/**
	 * 
	 * @return the topic of the subscription.
	 */
	public String getTopic() {
		
		return topic;
	}
	
	/**
	 * 
	 * @param id the subscription id to set.
	 */
	public void setId(String id) {
		
		this.id = id;
	}

	/**
	 * 
	 * @param topic the topic to set.
	 */
	public void setTopic(String topic) {
		this.topic = topic;

	}

    /**
     * Send data to subscribing endpoint.
     * @param eventData the message to be transformed and sent.
     * @throws Exception
     */
    public abstract void sendEventData(Message eventData) throws Exception;


}
