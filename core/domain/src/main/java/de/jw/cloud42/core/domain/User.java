/**
 * 
 */
package de.jw.cloud42.core.domain;


import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.UniqueConstraint;



import org.hibernate.annotations.Cascade;

/**
 * 
 * Represents an user (of the webapp).
 * 
 * @author fbitzer
 *
 */
@Entity
public class User extends AutoIdObject{
	
	
	private String name;
	private String password;
	
	private String regionUrl = null;
	
	public String getRegionUrl() {
		return regionUrl;
	}

	public void setRegionUrl(String regionUrl) {
		this.regionUrl = regionUrl;
	}

	private List<KeypairMapping> keys;
	
	
	/**
	 * the user's AWS credentials. 
	 */
	private AwsCredentials credentials;

	/**
	 * @return the name
	 */
	@Column(unique=true)
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the crendentials
	 */

	@OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@Cascade({org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
	public AwsCredentials getCredentials() {
		return credentials;
	}

	/**
	 * @param crendentials the crendentials to set
	 */
	public void setCredentials(AwsCredentials credentials) {
		this.credentials = credentials;
	}

	/**
	 * @return the keys
	 */
	@OneToMany(fetch=FetchType.EAGER)
	public List<KeypairMapping> getKeys() {
		return keys;
	}

	/**
	 * @param keys the keys to set
	 */
	public void setKeys(List<KeypairMapping> keys) {
		this.keys = keys;
	}
	
	
	
}
