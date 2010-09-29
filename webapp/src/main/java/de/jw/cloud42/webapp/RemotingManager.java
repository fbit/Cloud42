/**
 * 
 */
package de.jw.cloud42.webapp;

import java.io.File;

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
 * Seam component wrapper around remoting functions of Cloud42.
 * 
 * @author fbitzer
 *
 */
@Name("remotingManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout=1000000000)
public class RemotingManager {

	@In
	UserManager userManager;
	
	/**
	 * Inject faces messages to trigger error and success messages.
	 */
	@In 
	FacesMessages facesMessages;
	
	
	

	
	//input fields used in the view (remoting dialog)
	private String command;
	private boolean useBatch;
	private byte[] batchFile;
	private RemoteResult result;
	
	
	
	/**
	 * fileUploadListener function for RichFaces fileupload component.
	 * Reads the content of the uploaded file into a byte array and stores it in the
	 * <code>batchFile</code> property.
	 * @param e
	 */
	public void fileUploadListener(UploadEvent e){
		
		try {
			
			File f = e.getUploadItem().getFile();
						
			this.batchFile = FileUtils.getBytesFromFile(f);
			
			e.getUploadItem().getFile().delete();
		}
		catch (Exception ex) {
			Logger.getAnonymousLogger().warning("Uploading file failed: " + ex.getMessage());
		}
	}
	
	
	/**
	 * Execute a command or a batch file.
	 * @param dnsName Hostname of AMI.
	 * @param keyName Name of keypair to use.
	 */
	public void execute(String dnsName, String keyName){
		
		RemoteControl c = new RemoteControl();
		
		//get the private key for the keyname
		String privateKey = userManager.getKeyForName(keyName);
		
		
		
		if (useBatch && batchFile != null) {
			result = c.executeBatch(dnsName, privateKey, batchFile);
			batchFile = null;
		} else {
			result = c.executeCommand(dnsName, privateKey, command);
		}
		
		if (result == null) {
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_commandNotExecuted");
			result = new RemoteResult();//result must not be null because of databinding
		} else {
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_commandExecuted");
			
		}
		
	}
	
	/**
	 * Reset the remoting dialog by resetting bean values.
	 */
	public void resetRemotingDialog(){
		command = "";
		useBatch = false;
		batchFile = null;
		result = new RemoteResult();
	}

	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @return the useBatch
	 */
	public boolean isUseBatch() {
		return useBatch;
	}

	/**
	 * @param useBatch the useBatch to set
	 */
	public void setUseBatch(boolean useBatch) {
		this.useBatch = useBatch;
	}

	/**
	 * @return the batchFile
	 */
	public byte[] getBatchFile() {
		return batchFile;
	}

	/**
	 * @param batchFile the batchFile to set
	 */
	public void setBatchFile(byte[] batchFile) {
		this.batchFile = batchFile;
	}

	/**
	 * @return the result
	 */
	public RemoteResult getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(RemoteResult result) {
		this.result = result;
	}
	
}
