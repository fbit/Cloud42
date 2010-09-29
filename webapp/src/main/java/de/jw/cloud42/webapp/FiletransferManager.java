/**
 * 
 */
package de.jw.cloud42.webapp;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;


import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;

import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;

import org.jboss.seam.faces.FacesMessages;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;


import de.jw.cloud42.core.domain.RemoteResult;
import de.jw.cloud42.core.remoting.RemoteControl;
import de.jw.cloud42.webapp.utils.FileUtils;

/**
 * Seam component wrapper around file transfer functions of Cloud42.
 * 
 * @author fbitzer
 *
 */
@Name("filetransferManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout=1000000000)
public class FiletransferManager {

	@In
	UserManager userManager;
	
	/**
	 * Inject faces messages to trigger error and success messages.
	 */
	@In 
	FacesMessages facesMessages;
	
	
	//Inject FacesContext and externalContext to be able to create custom responses for file download
	@In(value="#{facesContext.externalContext}")
	private ExternalContext extCtx;
	@In(value="#{facesContext}")
	javax.faces.context.FacesContext facesContext;

	
	//input fields used in the view (filetransfer dialog)
	private String targetDirContent;
	private String targetDir;
	private boolean uploadFromURL;
	private String uploadURL;
	
	private List<UploadItem> files = new ArrayList<UploadItem>();
	

	private String targetFilename = "";
	
	
	
	private String downloadDirContent;
	private String downloadFilename;
	
	
	/**
	 * Uploads a file to an AMI instance.
	 * @param dnsName Hostname of instance.
	 * @param keyName Name of keypair to use. Actual key is retreived from stored private keys, if existing. 
	 */
	public void uploadFile(String dnsName, String keyName){
		
		//check input first
		if (!uploadFromURL) {
			if (this.files.size()<1){
				facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_invalidInput", "No file.");
				return;
			}
		} else {
			if (this.uploadURL.equals("")){
				facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_invalidInput", "No URL.");
				return;
			}
			
			
			if (this.targetFilename.equals("")){
				facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_invalidInput", "No target file.");
				return;
			}
		}
		
		RemoteControl c = new RemoteControl();
		
		//get the private key for the keyname
		String privateKey = userManager.getKeyForName(keyName);
		
		RemoteResult r = new RemoteResult();
		
		boolean success = false;
		
		//decide whether to upload a local file or a file from an URL
		if (this.uploadFromURL) {
			
			//adjust the target directory
			if (!targetDir.startsWith("/")) {
				targetDir = "~/" + targetDir;
			}
			
			r = c.uploadFileFromURL(dnsName, privateKey, targetDir, targetFilename, uploadURL);
			
			if (r.getExceptionMessage() == null && r.getExitCode() == 0) success = true;
		
		} else {
			
			//iterate through uploaded files
			
			for (UploadItem item : files){
				
				String filename = FileUtils.extractFilename(item.getFileName());
				
				try {
					byte[] filedata = FileUtils.getBytesFromFile(item.getFile());
					
					r = c.uploadFile(dnsName, privateKey, targetDir, filename, filedata);
					
					if (r.getExceptionMessage() == null && (r.getExitCode() == 0)){
						success = true;
					} else {
						success = false;
						break;
					}
				} catch (Exception ex){
					success = false;
					break;
				}
				
			}
			
			
		}
		
		if (!success){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_fileNotUploaded");
			Logger.getAnonymousLogger().severe("Error uploading file: " + r.getExceptionMessage());
		} else {
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_fileUploaded");
	
		}

		//reset file after upload (the directory is kept in case the user wants to upload another file
		//to the same dir)
		this.files = new ArrayList<UploadItem>();
		targetFilename="";
		
	}
	
	/**
	 * List the contents of a folder by remotely executing a ls -a.
	 * Stores the result in the corresponding member variable in order to provide access from the Facelets view.
	 * @param dnsName Hostname of instance.
	 * @param keyName Name of keypair to use.
	 * @param download boolean flag indicating whether the listing should be for file download or
	 * file upload area. In case of file download, a wildcard is inserted into the command (ls -a xyz*).
	 */
	public void listFolder(String dnsName, String keyName, boolean download){
		
		//get the private key for the keyname
		String privateKey = userManager.getKeyForName(keyName);
		
		String result = "";
		
		String dir="";
		if (!download){
			dir = targetDir;
		} else {
			dir = downloadFilename + "*";
		}
		
		if (privateKey != null){
			
			RemoteControl c = new RemoteControl();
			
			RemoteResult r = c.executeCommand(dnsName, privateKey, "ls -a " + dir);
			
			if (r.getExceptionMessage() != null){
				result =  "Exception: " + r.getExceptionMessage();
			} else if (!r.getStdErr().equals("")){
				result = "Error: " + r.getStdErr();
			} else {
				result =  r.getStdOut();
			}
		}
		
		if (!download){
			targetDirContent = result;
		} else {
			downloadDirContent = result;
		}
		
	}
	
	/**
	 * Resets the file dialog by resetting the member variables for displayment in the UI.
	 */
	public void resetFileDialog(){
		
		this.targetDirContent = "";
		targetDir = "";
		targetFilename = "";
		
		uploadFromURL = false;
		uploadURL = "";
		
		
		this.downloadDirContent="";
		downloadFilename="";
		
		this.clearUpload();
		
		
	}
	/**
	 * Resets the list of uploaded files.
	 */
	public void clearUpload(){
		files =  new ArrayList<UploadItem>();
	}
	
	
	/**
	 * Download a file from an AMI instance and send it to the client using HTTP.
	 * @param dnsName Hostname of instance.
	 * @param keyName Name of keypair to use.
	 */
	public void downloadFile(String dnsName, String keyName){
		

		//check input first
		if (this.downloadFilename.equals("")) {
			
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_invalidInput", "No file.");
			return;
			
		}
		
		RemoteControl c = new RemoteControl();
		
		//get the private key for the keyname
		String privateKey = userManager.getKeyForName(keyName);
		
		byte[] file = c.downloadFile(dnsName, privateKey, downloadFilename);
		
		if (file != null){
			//Create a HTTPResponse and send it back skipping the usual JSF lifecycle
			HttpServletResponse response = (HttpServletResponse)extCtx.getResponse();
			
			//response.setContentType(?);//content-type is unknown
			
			//the Header Content-Disposition causes a browser to display a download dialog instead
			//of trying to display the file directly. This way, content-type becomes irrelevant.
			//Use original filename as proposed filename for download dialog.
	        String fname = downloadFilename.split("/")[downloadFilename.split("/").length - 1];
			response.setHeader( "Content-Disposition", "attachment; filename=\""
					+ fname + "\";");
	
			//write file into response stream.
			try {
				ServletOutputStream os = response.getOutputStream();
				os.write(file);
				os.flush();
				os.close();
				facesContext.responseComplete();
			} catch(Exception e) {
				Logger.getAnonymousLogger().severe("Error serving downloaded file: " + e.getMessage());
			}
		} else {
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_fileNotDownloaded");
			Logger.getAnonymousLogger().severe("Error downloading file.");
		}

	}

	
	
	//getters and setters for bean properties...
	
	/**
	 * @return the targetDirContent
	 */
	public String getTargetDirContent() {
		return targetDirContent;
	}

	/**
	 * @param targetDirContent the targetDirContent to set
	 */
	public void setTargetDirContent(String targetDirContent) {
		this.targetDirContent = targetDirContent;
	}

	/**
	 * @return the userManager
	 */
	public UserManager getUserManager() {
		return userManager;
	}

	/**
	 * @param userManager the userManager to set
	 */
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	/**
	 * @return the targetDir
	 */
	public String getTargetDir() {
		return targetDir;
	}

	/**
	 * @param targetDir the targetDir to set
	 */
	public void setTargetDir(String targetDir) {
		this.targetDir = targetDir;
	}

	/**
	 * @return the targetFilename
	 */
	public String getTargetFilename() {
	
		return targetFilename;
	}

	/**
	 * @param targetFilename the targetFilename to set
	 */
	public void setTargetFilename(String targetFilename) {
		
		this.targetFilename = targetFilename;
	}

	/**
	 * @return the uploadFromURL
	 */
	public boolean isUploadFromURL() {
		return uploadFromURL;
	}

	/**
	 * @param uploadFromURL the uploadFromURL to set
	 */
	public void setUploadFromURL(boolean uploadFromURL) {
		this.uploadFromURL = uploadFromURL;
	}




	/**
	 * @return the uploadURL
	 */
	public String getUploadURL() {
		return uploadURL;
	}

	/**
	 * @param uploadURL the uploadURL to set
	 */
	public void setUploadURL(String uploadURL) {
		this.uploadURL = uploadURL;
	}

	/**
	 * @return the downloadFilename
	 */
	public String getDownloadFilename() {
		return downloadFilename;
	}

	/**
	 * @param downloadFilename the downloadFilename to set
	 */
	public void setDownloadFilename(String downloadFilename) {
		this.downloadFilename = downloadFilename;
	}

	/**
	 * @return the downloadDirContent
	 */
	public String getDownloadDirContent() {
		return downloadDirContent;
	}

	/**
	 * @param downloadDirContent the downloadDirContent to set
	 */
	public void setDownloadDirContent(String downloadDirContent) {
		this.downloadDirContent = downloadDirContent;
	}

	public List<UploadItem> getFiles() {
		return files;
	}

	public void setFiles(List<UploadItem> files) {
		this.files = files;
	}
}
