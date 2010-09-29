/**
 * 
 */
package de.jw.cloud42.core.domain;

import javax.persistence.Entity;

/**
 * Holds AWS credentials: AWSAccessKeyId, SecretAccessKey, UserID
 * 
 * @author fbitzer
 *
 */
@Entity
public class AwsCredentials extends AutoIdObject {
	private String awsAccessKeyId;
	private String secretAccessKey;
	private String userID;
	
	/**
	 * @return the aWSAccessKeyId
	 */
	public String getAwsAccessKeyId() {
		return awsAccessKeyId;
	}
	/**
	 * @param accessKeyId the aWSAccessKeyId to set
	 */
	public void setAwsAccessKeyId(String accessKeyId) {
		awsAccessKeyId = accessKeyId;
	}
	/**
	 * @return the secretAccessKey
	 */
	public String getSecretAccessKey() {
		return secretAccessKey;
	}
	/**
	 * @param secretAccessKey the secretAccessKey to set
	 */
	public void setSecretAccessKey(String secretAccessKey) {
		this.secretAccessKey = secretAccessKey;
	}
	/**
	 * @return the userID
	 */
	public String getUserID() {
		return userID;
	}
	/**
	 * @param userID the userID to set
	 */
	public void setUserID(String userID) {
		this.userID = userID;
	}
	
	
	
}
