/**
 * 
 */
package de.jw.cloud42.core.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Base class for all entities that have a auto generated id.
 * 
 * @author fbitzer
 *
 */
@MappedSuperclass
public class AutoIdObject {
	
	private int id;
	
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}


}
