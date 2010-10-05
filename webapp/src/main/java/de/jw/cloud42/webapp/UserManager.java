/**
 * 
 */
package de.jw.cloud42.webapp;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;

import de.jw.cloud42.core.domain.AwsCredentials;
import de.jw.cloud42.core.domain.KeypairMapping;
import de.jw.cloud42.core.domain.User;
import de.jw.cloud42.core.service.Cloud42Settings;

/**
 * Holds current user and credentials. Also contains logic for managing the user's RSA private keys.
 * 
 * @author fbitzer
 *
 */
@Name("userManager")
@Scope(ScopeType.SESSION)
public class UserManager {
	
	private User user = new User();
	
	/**
	 * current credentials. These are the credentials that are shown on top of each page.
	 */
	private AwsCredentials currentCredentials = new AwsCredentials();
	
	/**
	 * the injected Seam-managed Hibernate session
	 */
	@In 
	Session session;
	
	/**
	 * Inject faces messages to trigger error and success messages.
	 */
	@In 
	FacesMessages facesMessages;
	
	
	@In(create = true)
	BaseFunctionsManager baseFunctionsManager;

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * @return the currentCredentials
	 */
	public AwsCredentials getCurrentCredentials() {
		return currentCredentials;
	}

	/**
	 * @param currentCredentials the currentCredentials to set
	 */
	public void setCurrentCredentials(AwsCredentials currentCredentials) {
		this.currentCredentials = currentCredentials;
	}


	/**
	 * Creates a new User instance.
	 */
	public void newUser(){
		user = new User();
	}
	
	/**
	 * Saves a new user.
	 */
	public void createAccount(){
	
		//check for duplicated usernames etc.
		try {
		
			
			//set the default region for the user from the settins file
			Cloud42Settings config = Cloud42Settings.getInstance("config.properties");
			
			String regionUrl = config.getString("regionUrl");
			
			if (regionUrl != null && regionUrl != ""){
				user.setRegionUrl(regionUrl);
				Logger.getAnonymousLogger().info("Setting regionUrl for new user to '" + regionUrl + "'");
				
			}
			
			
			session.setFlushMode(FlushMode.COMMIT);
			
			session.beginTransaction();
			session.save(user);
			session.getTransaction().commit();
			
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_accountCreated");
			
		} catch (Exception ex){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_accountNotCreated");
		}
	}
	/**
	 * Saves current credentials by assigning them to the user.
	 */
	public void saveCredentials(){
		try {
			//reset all lists
			baseFunctionsManager.resetGroupList();
			baseFunctionsManager.resetImageList();
			baseFunctionsManager.resetInstanceList();
			baseFunctionsManager.resetKeypairList();
			baseFunctionsManager.resetPrivateKey();
			
			baseFunctionsManager.resetRegionList();
			
			
			user.setCredentials(currentCredentials);
			
			session.setFlushMode(FlushMode.COMMIT);
			
			session.beginTransaction();
			session.saveOrUpdate(user);
			session.getTransaction().commit();
			
		} catch (Exception ex){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_credentialsNotSaved");
		}
		
	}
	
	/**
	 * Saves current region.
	 */
	public void saveRegion(){
		
		
		session.setFlushMode(FlushMode.COMMIT);
		
		session.beginTransaction();
		session.saveOrUpdate(user);
		session.getTransaction().commit();
		
		//reset all the lists
		baseFunctionsManager.resetGroupList();
		baseFunctionsManager.resetImageList();
		baseFunctionsManager.resetInstanceList();
		baseFunctionsManager.resetKeypairList();
		baseFunctionsManager.resetPrivateKey();
		
		baseFunctionsManager.resetRegionList();
	}
	
	/**
	 * Stores a private key for a keypair in the database and maps it to the current user. 
	 * If there is already a key for a keypair with same name, it is overridden.
	 * @param keypairName name of the keypair.
	 * @param privateKey the private key to save.
	 */
	public void savePrivateKey(String keypairName, String privateKey){
		
		if (!privateKey.equals(BaseFunctionsManager.MSG_KEYPAIR_ERROR)){
		
			this.deletePrivateKey(keypairName);
			
			//update user in DB
			session.setFlushMode(FlushMode.COMMIT);
			session.beginTransaction();
			
			
			KeypairMapping newMapping = new KeypairMapping();
			
			newMapping.setKeypairName(keypairName);
			newMapping.setRsaPrivateKey(privateKey);
			
			user.getKeys().add(newMapping);
			
			session.saveOrUpdate(newMapping);
			
			session.saveOrUpdate(user);
			session.getTransaction().commit();
		
		}
		
	}
	/**
	 * Checks whether a private key for the keypair with given name exists.
	 * @param keypairName name of the keypair.
	 * @return true, if a private key for the keypair with given name exists, false else
	 */
	public boolean privateKeyExists(String keypairName){
		
		List<KeypairMapping> l = user.getKeys();
		
		for (KeypairMapping m : l){
			if (m.getKeypairName().equals(keypairName)){
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Delete a private key for a keypair.
	 * @param keypairName the name of the keypair.
	 */	
	public void deletePrivateKey(String keypairName){
		
		
		//delete a key
		List<KeypairMapping> l = user.getKeys();
		
		//list of keys to delete (in case there are multiple mappings with the same name)
		List<KeypairMapping> toDelete = new ArrayList<KeypairMapping>();
		
		for (KeypairMapping m : l){
			if (m.getKeypairName().equals(keypairName)){
				toDelete.add(m);
			}
		}
		
		session.setFlushMode(FlushMode.COMMIT);
		session.beginTransaction();
		
		for (KeypairMapping m : toDelete){
			user.getKeys().remove(m);
			session.delete(m);
		}
		
		
		session.saveOrUpdate(user);
		
		session.getTransaction().commit();
		
	}
	
	/**
	 * Retreive a previously saved private key.
	 * @param keyName the name of the keypair the key belongs to.
	 * @return RSA private key for given keypair or null if no key was found.
	 */
	public String getKeyForName(String keyName){
		
		List<KeypairMapping> l = user.getKeys();
		
		for (KeypairMapping m : l){
			if (m.getKeypairName().equals(keyName)){
				return m.getRsaPrivateKey();
			}
		}
		
		return null;
	}
}
