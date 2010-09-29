/**
 * 
 */
package de.jw.cloud42.webapp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.faces.FacesMessages;
import org.richfaces.event.UploadEvent;

import de.jw.cloud42.core.domain.RemoteResult;
import de.jw.cloud42.core.remoting.RemoteControl;
import de.jw.cloud42.webapp.utils.FileUtils;


/**
 * Seam component, handles bundling new AMIs and holds corresponding properties.
 * 
 * @author fbitzer
 *
 */
@Name("bundlingManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout=1000000000)
public class BundlingManager {

	@In
	UserManager userManager;
	
	/**
	 * Inject faces messages to trigger error and success messages.
	 */
	@In 
	FacesMessages facesMessages;
	
	
	private HashMap<String, BundlingThread> bundlingThreads = new HashMap<String, BundlingThread>();
	
	//the Member variables for the input fields in the UI
	private String bucket;
	private String imageName;
	private boolean use64Bit;
	
	private boolean doNotify;
	private String messageTopic;
	private String messageText;
	private String messageInfo;
	
	private byte[] certFile;
	private byte[] pkFile;
	
	/**
	 * Starts bundling a new AMI.
	 * 
	 * @param dnsName
	 * @param keyName
	 */
	public void bundle(String dnsName, String keyName){
		
		//check input first
		
		if (this.bucket.equals("")){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_invalidInput", "No bucket.");
			return;
		}
		
		if (this.certFile==null){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_invalidInput", "No certificate.");
			return;
		}
		if (this.pkFile==null){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_invalidInput", "No key file.");
			return;
		}
		
		if (this.imageName.equals("")){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_invalidInput", "No image name.");
			return;
		}
		
		
		
		//get the private key for the keyname
		String privateKey = userManager.getKeyForName(keyName);
		
		//create a thread for bundling
		BundlingThread t = new BundlingThread(dnsName, privateKey, userManager.getCurrentCredentials(),
				bucket, imageName, use64Bit, 
				doNotify, messageTopic, messageText, messageInfo, privateKey.getBytes(), certFile);

		
		t.start();
		
		//add the thread to the list of currently running threads
		bundlingThreads.put(dnsName, t);
		
		
		
	}
	
	/**
	 * Checks if bundling is in progress for a particular instance.
	 * @param dnsName Hostname of the instance to check
	 * 
	 * @return
	 */
	public boolean bundlingInProgress(String dnsName){
		
		//check if there is a thread attached to the hostname
		BundlingThread t = bundlingThreads.get(dnsName);
		
		
		if (t != null){
			//yes, there is a thread
			if (t.getState() == Thread.State.TERMINATED) {
				//its state is terminated so bundling has ended -> check the result
				if (t.getResult().getExitCode() != 0) {
					facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_bundlingError", t.getResult().getExceptionMessage());
				
				} else {
					facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_bundlingOK", dnsName);
					
				}
				//remove the ended thread
				bundlingThreads.remove(dnsName);
				
				return false;
			} else {
				//thread found, but not finished -> bundling is running
				return true;
			}
		} else {
			//no bundling thread found -> no bundling in progress
			return false;
		}
		
	}
	
	
	/**
	 * Resets the dialog by resetting the member variables.
	 */
	public void resetBundlingDialog(){
		bucket = "";
		imageName ="";
		use64Bit = false;
		certFile = null;
		pkFile = null;
		
		doNotify = false;
		messageTopic = "";
		messageText = "";
		messageInfo = "";
	}
	
	/**
	 * fileUploadListener function for RichFaces fileupload component.
	 * Reads the content of the uploaded file into a byte array and stores it in the
	 * <code>certFile</code> property.
	 * @param e
	 */
	public void fileUploadListener(UploadEvent e){
		
		this.certFile = getFile(e);
			
	}
	
	/**
	 * fileUploadListener function for RichFaces fileupload component.
	 * Reads the content of the uploaded file into a byte array and stores it in the
	 * <code>pkFile</code> property.
	 * @param e
	 */
	public void fileUploadListenerPK(UploadEvent e){
		
		this.pkFile = getFile(e);
		
	}
	
	
	//-------Getters and Setters---------------
	/**
	 * @return the bucket
	 */
	public String getBucket() {
		return bucket;
	}
	/**
	 * @param bucket the bucket to set
	 */
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	/**
	 * @return the imageName
	 */
	public String getImageName() {
		return imageName;
	}
	/**
	 * @param imageName the imageName to set
	 */
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	/**
	 * @return the use64Bit
	 */
	public boolean isUse64Bit() {
		return use64Bit;
	}
	/**
	 * @param use64Bit the use64Bit to set
	 */
	public void setUse64Bit(boolean use64Bit) {
		this.use64Bit = use64Bit;
	}
	/**
	 * @return the doNotify
	 */
	public boolean isDoNotify() {
		return doNotify;
	}
	/**
	 * @param doNotify the doNotify to set
	 */
	public void setDoNotify(boolean doNotify) {
		this.doNotify = doNotify;
	}
	/**
	 * @return the messageTopic
	 */
	public String getMessageTopic() {
		return messageTopic;
	}
	/**
	 * @param messageTopic the messageTopic to set
	 */
	public void setMessageTopic(String messageTopic) {
		this.messageTopic = messageTopic;
	}
	/**
	 * @return the messageText
	 */
	public String getMessageText() {
		return messageText;
	}
	/**
	 * @param messageText the messageText to set
	 */
	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
	/**
	 * @return the messageInfo
	 */
	public String getMessageInfo() {
		return messageInfo;
	}
	/**
	 * @param messageInfo the messageInfo to set
	 */
	public void setMessageInfo(String messageInfo) {
		this.messageInfo = messageInfo;
	}



	/**
	 * @return the certFile
	 */
	public byte[] getCertFile() {
		return certFile;
	}



	/**
	 * @param certFile the certFile to set
	 */
	public void setCertFile(byte[] certFile) {
		this.certFile = certFile;
	}



	/**
	 * @return the pkFile
	 */
	public byte[] getPkFile() {
		return pkFile;
	}



	/**
	 * @param pkFile the pkFile to set
	 */
	public void setPkFile(byte[] pkFile) {
		this.pkFile = pkFile;
	}


	/**
	 * Private helper function, extracts an uploaded private key or certificate file.
	 * @param e RichFaces upload event
	 * @return
	 */
	private byte[] getFile(UploadEvent e){
		try {
			File f = e.getUploadItem().getFile();
			
			byte[] result = FileUtils.getBytesFromFile(f);
			
			e.getUploadItem().getFile().delete();
			
			return result;
		
		} catch (Exception ex) {
			Logger.getAnonymousLogger().warning("Uploading file failed: " + ex.getMessage());
		}
		return null;
	}
	
	
	
	
}
