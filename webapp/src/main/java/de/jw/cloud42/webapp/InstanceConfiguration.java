/**
 * 
 */
package de.jw.cloud42.webapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.richfaces.event.UploadEvent;

import de.jw.cloud42.webapp.utils.FileUtils;



/**
 * 
 * Backing bean for instance configuration dialog. Holds values entered in the UI and is
 * injected into the BaseFunctionsManager when a new instance is launched.
 * 
 * @author fbitzer
 *
 */
@Name("instanceConfiguration")
@Scope(ScopeType.SESSION)
public class InstanceConfiguration{
	
	private String imageId;
	
	private String type;
	
	private String name;

	private String keypairName;
	
	
	private String userData;
	
	private byte[] attachedFile;
	
	private List<String> groupNames;
	
	
	private String availabilityZone;
	private String kernelId;
	private String ramdiskId;
	
	
	private String imageLocation;

	
	/**
	 * @return the imageLocation
	 */
	public String getImageLocation() {
		return imageLocation;
	}

	/**
	 * @param imageLocation the imageLocation to set
	 */
	public void setImageLocation(String imageLocation) {
		this.imageLocation = imageLocation;
	}

	/**
	 * @return the userData
	 */
	public String getUserData() {
		return userData;
	}

	/**
	 * @param userData the userData to set
	 */
	public void setUserData(String userData) {
		this.userData = userData;
	}

	/**
	 * @return the groupNames
	 */
	public List<String> getGroupNames() {
		return groupNames;
	}

	/**
	 * @param groupNames the groupNames to set
	 */
	public void setGroupNames(List<String> groupNames) {
		this.groupNames = groupNames;
	}

	/**
	 * @param keypairName the keypairName to set
	 */
	public void setKeypairName(String keypairName) {
		this.keypairName = keypairName;
	}

	/**
	 * @return the instanceType
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param instanceType the instanceType to set
	 */
	public void setType(String instanceType) {
		this.type = instanceType;
	}

	/**
	 * @return the instanceName
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param instanceName the instanceName to set
	 */
	public void setName(String instanceName) {
		this.name = instanceName;
	}

	/**
	 * @return the imageId
	 */
	public String getImageId() {
		return imageId;
	}

	/**
	 * @param imageId the imageId to set
	 */
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	/**
	 * @return the keypair
	 */
	public String getKeypairName() {
		return keypairName;
	}

	/**
	 * @return the availabilityZone
	 */
	public String getAvailabilityZone() {
		return availabilityZone;
	}

	/**
	 * @param availabilityZone the availabilityZone to set
	 */
	public void setAvailabilityZone(String availabilityZone) {
		this.availabilityZone = availabilityZone;
	}

	/**
	 * @return the kernelId
	 */
	public String getKernelId() {
		return kernelId;
	}

	/**
	 * @param kernelId the kernelId to set
	 */
	public void setKernelId(String kernelId) {
		this.kernelId = kernelId;
	}

	/**
	 * @return the ramdiskId
	 */
	public String getRamdiskId() {
		return ramdiskId;
	}

	/**
	 * @param ramdiskId the ramdiskId to set
	 */
	public void setRamdiskId(String ramdiskId) {
		this.ramdiskId = ramdiskId;
	}

	/**
	 * @return the attachedFile
	 */
	public byte[] getAttachedFile() {
		return attachedFile;
	}

	/**
	 * @param attachedFile the attachedFile to set
	 */
	public void setAttachedFile(byte[] attachedFile) {
		this.attachedFile = attachedFile;
	}

	

	
	
	/**
	 * fileUploadListener function for RichFaces fileupload component.
	 * Reads the content of the uploaded file into a byte array and stores it in the
	 * <code>attachedFile</code> property.
	 * @param e
	 */
	public void fileUploadListener(UploadEvent e){
		
		
		try {
			
			File f = e.getUploadItem().getFile();
						
			this.attachedFile = FileUtils.getBytesFromFile(f);
			
			
			e.getUploadItem().getFile().delete();
		}
		catch (Exception ex) {
			Logger.getAnonymousLogger().warning("Attaching file failed: " + ex.getMessage());
		}
	}

}
