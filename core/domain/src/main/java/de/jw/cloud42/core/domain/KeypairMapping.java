/**
 * 
 */
package de.jw.cloud42.core.domain;

import javax.persistence.Entity;
import javax.persistence.UniqueConstraint;

/**
 * @author fbitzer
 *
 */
@Entity
public class KeypairMapping extends AutoIdObject {

	private String keypairName;
	private String rsaPrivateKey;
	
	
	/**
	 * @return the keypairName
	 */
	public String getKeypairName() {
		return keypairName;
	}
	/**
	 * @param keypairName the keypairName to set
	 */
	public void setKeypairName(String keypairName) {
		this.keypairName = keypairName;
	}
	/**
	 * @return the rsaPrivateKey
	 */
	public String getRsaPrivateKey() {
		return rsaPrivateKey;
	}
	/**
	 * @param rsaPrivateKey the rsaPrivateKey to set
	 */
	public void setRsaPrivateKey(String rsaPrivateKey) {
		this.rsaPrivateKey = rsaPrivateKey;
	}
	
	
	
	
}
