/**
 * 
 */
package de.jw.cloud42.core.service;

import com.xerox.amazonws.ec2.Jec2;

import de.jw.cloud42.core.domain.AwsCredentials;

/**
 * Base class for all functions that the service layer provides.
 * 
 * @author fbitzer
 *
 */
public abstract class Cloud42Functions {
	
	/**
	 * Instance of the Typica interface.
	 */
	protected Jec2 ec2;
	
	/**
	 * User AWS credentials
	 */
	private AwsCredentials credentials;
	
	/*
	 * The region to use
	 */
	private String regionUrl;

	public String getRegionUrl() {
		return regionUrl;
	}



	public void setRegionUrl(String regionUrl) {
		this.regionUrl = regionUrl;
	}



	/**
	 * @return the credentials
	 */
	public AwsCredentials getCredentials() {
		return credentials;
	}



	/**
	 * @param credentials the credentials to set
	 */
	public void setCredentials(AwsCredentials credentials) {
		this.credentials = credentials;
		
		
		
	}

	
}
