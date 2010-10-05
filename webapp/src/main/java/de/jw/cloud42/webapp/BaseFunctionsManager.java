/**
 * 
 */
package de.jw.cloud42.webapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.faces.FacesMessages;

import com.xerox.amazonws.ec2.AvailabilityZone;
import com.xerox.amazonws.ec2.ConsoleOutput;
import com.xerox.amazonws.ec2.GroupDescription;
import com.xerox.amazonws.ec2.ImageDescription;
import com.xerox.amazonws.ec2.InstanceStateChangeDescription;
import com.xerox.amazonws.ec2.InstanceType;
import com.xerox.amazonws.ec2.KeyPairInfo;
import com.xerox.amazonws.ec2.RegionInfo;
import com.xerox.amazonws.ec2.ReservationDescription;
import com.xerox.amazonws.ec2.TerminatingInstanceDescription;
import com.xerox.amazonws.ec2.GroupDescription.IpPermission;



import de.jw.cloud42.core.domain.AwsCredentials;
import de.jw.cloud42.core.domain.Instance;
import de.jw.cloud42.core.service.Cloud42BaseFunctions;
import de.jw.cloud42.webapp.tree.GroupTreeBean;

/**
 * Seam component wrapper around Cloud42BaseFunctions.
 * 
 * @author fbitzer
 * 
 */
@Name("baseFunctionsManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout=1000000000)
public class BaseFunctionsManager {

	public final static String MSG_KEYPAIR_ERROR = "An error creating the keypair occurred! "
		+ "Maybe a keypair with the same name already exists. It is not possible to save a key.";
	
	
	private final long MAX_FILESIZE = 16000;
	
		
	@In
	private UserManager userManager;
	
	@Out
	private String privateKey = "Please wait a second...";
	// @Out
	// private boolean pairNotCreated = false;
	// private String keypairName = "";
	
	/**
	 * Configuration of a instance that should be started.
	 */
	@In(create=true)
	@Out
	private InstanceConfiguration instanceConfiguration;
	
	
	@In(create=true)
	@Out
	private PermissionConfiguration permissionConfiguration;
	
	/**
	 * Inject faces messages to trigger error and success messages.
	 */
	@In 
	FacesMessages facesMessages;

	@In(create=true)
	@Out
	GroupTreeBean groupTreeBean;
	
	@Out
	private String consoleOutput = "Please wait a second...";
	

	
	// List of images, instances, keywords, groups.
	private List<ImageDescription> imageList = null;
	
	private List<Instance> instanceList = null;
	
	private List<KeyPairInfo> keypairList = null;
	
	private List<GroupDescription> groupList = null;
	
	private List<AvailabilityZone> availabilityZoneList = null;
	
	private List<RegionInfo> regionList = null;
	
	
	// hashtable saves a name for each instance (identified by reservationId as
	// key)
	// @In(create=true)
	// @Out(scope=ScopeType.APPLICATION)
	// it is outjected to application scope so it is available during various
	// sessions.
	// Saving it to database (coupled to the user) might be the better
	// solution...
	// Feature is currently not used.
	private HashMap<String, String> instanceNames = new HashMap<String, String>();

	
	/**
	 * Reset imageList and force reload next time it is accessed.
	 */
	public void resetImageList(){
		this.imageList = null;
	}
	
	/**
	 * Reset instanceList and force reload next time it is accessed.
	 */
	public void resetInstanceList(){
		this.instanceList = null;
	}
	
	/**
	 * Reset groupList and force reload next time it is accessed.
	 */
	public void resetGroupList(){
		
		groupTreeBean.resetGroupList();
		
		this.groupList = null;
	}
	
	/**
	 * Reset keypairList and force reload next time it is accessed.
	 */
	public void resetKeypairList(){
	
		this.keypairList = null;
	}
	
	/**
	 * Reset regionList and force reload next time it is accessed.
	 */
	public void resetRegionList(){
		this.regionList = null;
	}
	
	
//	/**
//	 * Set the AWS Region.
//	 * @param regionUrl
//	 */
//	public void setRegionUrl(String regionUrl){
//		
//		Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
//		
//		
//		AwsCredentials cred = userManager.getUser().getCredentials();
//		if (cred!=null){
//			bf.setCredentials(cred);
//			
//			bf.setRegionUrl(regionUrl);
//		}
//		
//	}
	
	/**
	 * List all AMIs.
	 * 
	 * @return null if no AMIs were found (because of missing or wrong
	 *         credentials)
	 */
	public List<ImageDescription> getImages(){
		
		
		if (imageList == null){
			Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
			
			
			AwsCredentials cred = userManager.getUser().getCredentials();
			if (cred!=null){
				
				bf.setCredentials(cred);
			
				bf.setRegionUrl(userManager.getUser().getRegionUrl());
				
				
				ImageDescription[] list =  bf.listImages(true);
				if (list!=null){
					imageList = Arrays.asList(list);
				}
				
				
			}
		
//			if (imageList==null){
//				facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_imagesNotListed");
//			} else {
//				facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_imagesListed");
//			}
		
		
		}
		
		return imageList;
	}
	
	
	/**
	 * List all active instances.
	 * 
	 * @return null if no instances were found
	 */
	public List<de.jw.cloud42.core.domain.Instance> getInstances(){
		
		
		if (instanceList == null){
			Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
			
			
			
			
			AwsCredentials cred = userManager.getUser().getCredentials();
			if (cred!=null){
				bf.setCredentials(cred);
			
				bf.setRegionUrl(userManager.getUser().getRegionUrl());
				
				de.jw.cloud42.core.domain.Instance[] list =  bf.listInstances();
				if (list!=null){
					instanceList = Arrays.asList(list);
				}
			}
		
		
		
		}
		
		return instanceList;
	}
	
	/**
	 * List all keypairs.
	 * 
	 * @return null if no keypairs were found.
	 */
	public List<KeyPairInfo> getKeypairs(){
		
		
		if (keypairList == null){
			Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
			
			
			
			
			AwsCredentials cred = userManager.getUser().getCredentials();
			if (cred!=null){
				bf.setCredentials(cred);
				bf.setRegionUrl(userManager.getUser().getRegionUrl());
				
				KeyPairInfo[] list =  bf.listKeypairs();
				if (list!=null){
					keypairList = Arrays.asList(list);
				}
			}
		
		
		
		}
		
		return keypairList;
	}
	
	/**
	 * List all Regions.
	 * 
	 * @return null if no regions were found.
	 */
	public List<RegionInfo> getRegions(){
		
		
		if (regionList == null){
			Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
			
			AwsCredentials cred = userManager.getUser().getCredentials();
			if (cred!=null){
				bf.setCredentials(cred);
			
				//Do NOT set a special region here!
				bf.setRegionUrl(null);
				
				RegionInfo[] list =  bf.listRegions();
				if (list!=null){
					regionList = Arrays.asList(list);
				}
			}
		
		
		
		}
		
		return regionList;
	}
	
	/**
	 * List all groups.
	 * 
	 * @return null if no groups were found.
	 */
	public List<GroupDescription> getGroups(){
		// GroupDescription d;
		
		
		if (groupList == null){
			Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
			
			
			
			
			AwsCredentials cred = userManager.getUser().getCredentials();
			if (cred!=null){
				bf.setCredentials(cred);
				bf.setRegionUrl(userManager.getUser().getRegionUrl());
				
				GroupDescription[] list =  bf.listSecurityGroups();
				if (list!=null){
					groupList = Arrays.asList(list);
				}
			}
		
		
		
		}
		
		return groupList;
	}
	
	
	
	/**
	 * Return keypairs as list of SelectItems for displaying in a dropdown box.
	 * 
	 * @return
	 */
	public List<SelectItem> getKeypairItems(){
		
		List<SelectItem> result = new ArrayList<SelectItem>();
		
		List<KeyPairInfo> kl = this.getKeypairs();
		if (kl!=null){
			for (KeyPairInfo k : kl){
				
				result.add(new SelectItem(k.getKeyName(),k.getKeyName()));
			}
		}
		return result;
		
	}
	
	/**
	 * Return groups as list of SelectItems for displaying in a dropdown box.
	 * 
	 * @return
	 */
	public List<SelectItem> getGroupItems(){
		
		List<SelectItem> result = new ArrayList<SelectItem>();
		
		List<GroupDescription> gl = this.getGroups();
		if (gl!=null){
			for (GroupDescription d : gl){
				result.add(new SelectItem(d.getName(),d.getName()));
			}
		}
		return result;
		
	}
	
	
	/**
	 * Return possible instanceTypes as SelectItems.
	 * 
	 * @return
	 */
	public List<SelectItem> getInstanceTypeItems(){
		
		List<SelectItem> result = new ArrayList<SelectItem>();
		
		
		
		for (InstanceType t : InstanceType.values()){
			result.add(new SelectItem(t.getTypeId(),t.getTypeId()));
		}
		
		return result;
		
	}
	
	
	/**
	 * Return keypairs as list of SelectItems for displaying in a dropdown box.
	 * 
	 * @return
	 */
	public List<SelectItem> getRegionItems(){
		
		List<SelectItem> result = new ArrayList<SelectItem>();
		
		List<RegionInfo> rl = this.getRegions();
		if (rl!=null){
			for (RegionInfo i : rl){
				
				result.add(new SelectItem(i.getUrl(),i.getName()));
			}
		}
		return result;
		
	}
	
	
	/**
	 * Helper function, resets current instanceConfiguration and initializes
	 * values.
	 * 
	 * @param imageId
	 * @param imageLocation
	 */
	public void createNewConfiguration(String imageId, String imageLocation){
		instanceConfiguration = new InstanceConfiguration();
		
		instanceConfiguration.setImageId(imageId);
		instanceConfiguration.setImageLocation(imageLocation);
		
		
	}
	
	/**
	 * Starts an instance. Uses injected instanceConfiguration to set
	 * properties.
	 */
	public void runInstance(){
		
		if (instanceConfiguration.getAttachedFile() != null && 
				instanceConfiguration.getAttachedFile().length > MAX_FILESIZE){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_fileTooLarge");
			return;
		}
		
		Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
		
		
		
		ReservationDescription d=null;
		
		AwsCredentials cred = userManager.getUser().getCredentials();
		if (cred!=null){
			bf.setCredentials(cred);
			bf.setRegionUrl(userManager.getUser().getRegionUrl());
			
			InstanceType t = InstanceType.getTypeFromString(instanceConfiguration.getType());
			
			
			// if a file was attached, upload file
			// else use string provided as userdata
			byte[] userdata;
			if (instanceConfiguration.getAttachedFile() != null){
				
				userdata = instanceConfiguration.getAttachedFile();
				
			} else {
				userdata = instanceConfiguration.getUserData().getBytes();
			}
			
			d = bf.runInstance(instanceConfiguration.getImageId(), 
					instanceConfiguration.getGroupNames().toArray(new String[0]), 
					instanceConfiguration.getKeypairName(), 
					userdata,
					t,
					1,
					instanceConfiguration.getAvailabilityZone(),
					instanceConfiguration.getKernelId(),
					instanceConfiguration.getRamdiskId());	
			
			if (d!=null){
				// set name for the instance
				this.setName(d.getReservationId(), instanceConfiguration.getName());
				
				// force reload of list of current instances
				this.resetInstanceList();
			}
			
		}
		
		if (d==null){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_instanceNotStarted");
		} else {
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_instanceStarted",new Object[]{d.getInstances().get(0).getInstanceId()});
		}
			
		
	}
	
	/**
	 * Configures a new instance with the same configuration as the given
	 * instance. Does not start the instance.
	 * 
	 * @param instanceId
	 *            InstanceId of the instance from which the configuration should
	 *            be taken.
	 */
	public void runAnotherInstance(String instanceId){
		
		
		// describe the instance first and load its data into a
		// instanceConfiguration
		Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
		
		
		AwsCredentials cred = userManager.getUser().getCredentials();
		if (cred!=null){
			bf.setCredentials(cred);
			bf.setRegionUrl(userManager.getUser().getRegionUrl());
			
			Instance i = bf.describeInstance(instanceId);
		
			this.createNewConfiguration(i.getImageId(), "");
			
			this.instanceConfiguration.setGroupNames(Arrays.asList(i.getGroups()));
			this.instanceConfiguration.setKeypairName(i.getKeyName());
			this.instanceConfiguration.setName(getName(i));
			this.instanceConfiguration.setType(i.getInstanceType());
			
			this.instanceConfiguration.setAvailabilityZone(i.getAvailabilityZone());
			this.instanceConfiguration.setKernelId(i.getKernelId());
			this.instanceConfiguration.setRamdiskId(i.getRamdiskId());
			
			//userdata is not transferred!
			
			
		}
	}
	

	
	/**
	 * Get the user defined name for an instance.
	 * 
	 * @param instance
	 * @return
	 */
	public String getName(Instance instance){
		return instanceNames.get(instance.getReservationId());
	}
	
	/**
	 * Set the user defined name for an instance.
	 * 
	 * @param instance
	 * @param name
	 */
	public void setName(String reservationId, String name){
		instanceNames.put(reservationId, name);
	}
	
	/**
	 * Register a new AMI at given location.
	 * 
	 * @param location
	 */
	public void registerImage(String location){
		Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
		
		String id = null;
	
		
		AwsCredentials cred = userManager.getUser().getCredentials();
		if (cred!=null){
			bf.setCredentials(cred);
			bf.setRegionUrl(userManager.getUser().getRegionUrl());
			
			id = bf.registerImage(location);
			
			// reset list of images to force reload
			if (id!=null) this.resetImageList();
		}
		
		
		
		if (id==null){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_imageNotRegistered");
		} else {
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_imageRegistered",new Object[]{id});
		}
		
	}
	
	/**
	 * Deregisters an image.
	 * 
	 * @param image
	 */
	public void deregisterImage(ImageDescription image){
		Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
		
		
		
		boolean result = false;
		
		AwsCredentials cred = userManager.getUser().getCredentials();
		if (cred!=null){
			bf.setCredentials(cred);
			bf.setRegionUrl(userManager.getUser().getRegionUrl());
			
			result = bf.deregisterImage(image.getImageId());
			
			if (result) this.resetImageList();
		}
		
		if (!result){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_imageNotDeregistered");
		} else {
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_imageDeregistered");
		}
	}
	
	/**
	 * Stop an instance.
	 * 
	 * @param instanceId
	 *            Id if instance to shutdown.
	 */
	public void stopInstance(String instanceId){
		Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
		
		
		
		InstanceStateChangeDescription result = null;
		
		AwsCredentials cred = userManager.getUser().getCredentials();
		if (cred!=null){
			bf.setCredentials(cred);
			bf.setRegionUrl(userManager.getUser().getRegionUrl());

			result = bf.stopInstance(instanceId);
			
			if (result!=null) this.resetInstanceList();
		}
		
		if (result==null){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_instanceNotTerminated", new Object[]{instanceId});
		} else {
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_instanceTerminated", new Object[]{instanceId});
		}
	}
	
	/**
	 * Stop all running instances.
	 */
	public void stopAllInstances(){
		Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
		
		
		TerminatingInstanceDescription[] result = null;
		
		AwsCredentials cred = userManager.getUser().getCredentials();
		if (cred!=null){
			bf.setCredentials(cred);
			bf.setRegionUrl(userManager.getUser().getRegionUrl());
			
			result = bf.stopAllInstances();
			
			if (result!=null) this.resetInstanceList();
		}
		
		if (result==null){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_allInstancesNotTerminated");
		} else {
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_allInstancesTerminated");
		}
	}
	
	/**
	 * Reboot an instance.
	 * 
	 * @param instanceId
	 *            Id if instance to reboot.
	 */
	public void rebootInstance(String instanceId){
		
		Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
	
		AwsCredentials cred = userManager.getUser().getCredentials();
		if (cred!=null){
			bf.setCredentials(cred);
			bf.setRegionUrl(userManager.getUser().getRegionUrl());
			
			bf.rebootInstance(instanceId);
			
			
			this.resetInstanceList();
			
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_instanceRebooted", new Object[]{instanceId});
		} else {

			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_instanceNotRebooted", new Object[]{instanceId});
		}
		
	}
	
	
	
	/**
	 * Helper functions, gets the current timezone to display dates correctly.
	 * 
	 * @return
	 */
	public TimeZone getTimeZone(){
		
		return TimeZone.getDefault();
		
	}
	
	
	/**
	 * Create a new security group
	 */
	public void createGroup(String name, String description){
		
		Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
		
		boolean result = false;
		
		AwsCredentials cred = userManager.getUser().getCredentials();
		if (cred!=null){
			bf.setCredentials(cred);
			bf.setRegionUrl(userManager.getUser().getRegionUrl());
			
			result = bf.createSecurityGroup(name, description);
			
			if (result) this.resetGroupList();
		}
		
		if (!result){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_groupNotCreated");
		} else {
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_groupCreated",new Object[]{name});
		}

	}
	
	/**
	 * Delete a security group
	 */
	public void deleteGroup(String name){
		
		Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
		
		boolean result = false;
		
		AwsCredentials cred = userManager.getUser().getCredentials();
		if (cred!=null){
			bf.setCredentials(cred);
			bf.setRegionUrl(userManager.getUser().getRegionUrl());
			
			result = bf.deleteSecurityGroup(name);
			
			if (result) this.resetGroupList();
		}
		
		if (!result){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_groupNotDeleted");
		} else {
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_groupDeleted",new Object[]{name});
		}

	}
	
	/**
	 * Remove a permission.
	 * 
	 * @param group
	 * @param permission
	 */
	public void removePermission(){
		
		GroupDescription group = groupTreeBean.getSelectedGroup();
		IpPermission permission = groupTreeBean.getSelectedPermission();

		Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
		
		boolean result = false;
		
		AwsCredentials cred = userManager.getUser().getCredentials();
		if (permission != null && cred!=null){
			
			bf.setCredentials(cred);
			bf.setRegionUrl(userManager.getUser().getRegionUrl());
			
			result = true;
			
			if (permission.getIpRanges() != null && permission.getIpRanges().size()>0){
				for (String cidr : permission.getIpRanges()){
					
					if (! bf.removePermission(group.getName(), permission.getProtocol(), 
						permission.getFromPort(), permission.getToPort(), cidr)) {
						result = false;
					}
				}
			} else {
				// this is the "second group" mode
				for (String[] pair : permission.getUidGroupPairs())
				
				 if (! bf.removePermission(group.getName(),pair[1], pair[0])){
					 result = false;
				 }
			}
			
		}
		
		if (!result){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_permissionNotDeleted");
		} else {
			
			
			this.resetGroupList();
			
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_permissionDeleted");
		}

	}
	
	/**
	 * Initializes a new permission.
	 */
	public void createNewPermission() {
		
		
		GroupDescription group = groupTreeBean.getSelectedGroup();
		if (group != null){
			permissionConfiguration = new PermissionConfiguration();
			permissionConfiguration.setEditedGroup(group.getName());
		}
		
	}
	
	/**
	 * Add a permission that was configured using a permissionConfiguration.
	 */
	public void addPermission(){
		
		
		
		Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
		
		boolean result = false;
		
		AwsCredentials cred = userManager.getUser().getCredentials();
		if (permissionConfiguration != null && cred!=null){
			
			bf.setCredentials(cred);
			bf.setRegionUrl(userManager.getUser().getRegionUrl());
			
			String group = permissionConfiguration.getEditedGroup();
			
			if (permissionConfiguration.getSource().equals("cidr")){
				
				result = bf.addPermission(group,permissionConfiguration.getProtocol(),
						permissionConfiguration.getFromPort().intValue(),permissionConfiguration.getToPort().intValue(),
						permissionConfiguration.getCidr());
				
			} else {
				
				result = bf.addPermission(group, permissionConfiguration.getGroup(), 
						permissionConfiguration.getUser());
			}
			
		}
		
		if (!result){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_permissionNotAdded");
		} else {
			
			
			this.resetGroupList();
			
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_permissionAdded");
		}
		
		
	}
	
	/**
	 * Create a keypair and outject private key in component "privateKey".
	 * 
	 * @param name
	 */
	public void createKeypair(String name){
		
		Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
		
		KeyPairInfo ki = null;
		
		privateKey = MSG_KEYPAIR_ERROR;
		
		
		AwsCredentials cred = userManager.getUser().getCredentials();
		if (permissionConfiguration != null && cred!=null){
			
			bf.setCredentials(cred);
			bf.setRegionUrl(userManager.getUser().getRegionUrl());
			
			ki = bf.createKeypair(name);
			
			if (ki != null){
				privateKey = ki.getKeyMaterial();
			}
		}
		
		if (ki == null){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_keypairNotAdded");
			
		} else {
			
			
			this.resetKeypairList();
			
			// keypairName = name;
			
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_keypairAdded");
			
			
		}
		
	}
	
	/**
	 * Reset the private key.
	 */
	public void resetPrivateKey(){
		privateKey = "Please wait a second...";
	}
	
	public void deleteKeypair(KeyPairInfo kp){
	
		Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
		
		boolean result = false;
		
		AwsCredentials cred = userManager.getUser().getCredentials();
		if (permissionConfiguration != null && cred!=null){
			
			bf.setCredentials(cred);
			bf.setRegionUrl(userManager.getUser().getRegionUrl());
			
			result = bf.deleteKeypair(kp.getKeyName());
	
		}
		
		if (!result){
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "msg_keypairNotDeleted");
		} else {
			
			// delete mappings
			userManager.deletePrivateKey(kp.getKeyName());
			
			this.resetKeypairList();
			
			facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "msg_keypairDeleted");
		}
		
		
	}
	
	/**
	 * Return availability zones as list of SelectItems for displaying in a
	 * dropdown box.
	 * 
	 * @return
	 */
	public List<SelectItem> getAvailabilityZoneItems(){
		
		
		List<SelectItem> result = new ArrayList<SelectItem>();
		
		// add a "any" item
		SelectItem anyItem = new SelectItem("","<any>");
		result.add(anyItem);
		
		// Now list real availability zonesif not already in cache
		if (availabilityZoneList==null){
			
			AwsCredentials cred = userManager.getUser().getCredentials();
			if (cred!=null){
				Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
				
				bf.setCredentials(cred);
				bf.setRegionUrl(userManager.getUser().getRegionUrl());
				
				AvailabilityZone[] list =  bf.listAvailabilityZones();
				if (list!=null){
					availabilityZoneList = Arrays.asList(list);
				}
			}
		}	
		if (availabilityZoneList != null) {
			for (AvailabilityZone z : availabilityZoneList) {
				SelectItem item = new SelectItem();
				item.setLabel(z.getName() + " (" + z.getState() + ")");
				item.setValue(z.getName());
						
				result.add(item);
				
			}
		}
		return result;
		
	}
	
	/**
	 * Read console output for an instance and assign it to outjected variable.
	 * 
	 * @param instanceId
	 */
	public String getConsoleOutput(String instanceId){
		
		Cloud42BaseFunctions bf = new Cloud42BaseFunctions();
		
		
		AwsCredentials cred = userManager.getUser().getCredentials();
		if (cred!=null){
			
			bf.setCredentials(cred);
			bf.setRegionUrl(userManager.getUser().getRegionUrl());
			
			ConsoleOutput o = bf.getConsoleOutput(instanceId);
			
			if (o != null) {
				
				consoleOutput = o.getOutput();
				
			}
			
		}
		
		return "consoleOutput";
	}
	
}
