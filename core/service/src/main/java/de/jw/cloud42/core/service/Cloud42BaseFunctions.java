package de.jw.cloud42.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.xerox.amazonws.ec2.AvailabilityZone;
import com.xerox.amazonws.ec2.ConsoleOutput;
import com.xerox.amazonws.ec2.GroupDescription;
import com.xerox.amazonws.ec2.ImageDescription;
import com.xerox.amazonws.ec2.InstanceStateChangeDescription;
import com.xerox.amazonws.ec2.InstanceType;
import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.KeyPairInfo;
import com.xerox.amazonws.ec2.LaunchConfiguration;
import com.xerox.amazonws.ec2.RegionInfo;
import com.xerox.amazonws.ec2.ReservationDescription;
import com.xerox.amazonws.ec2.TerminatingInstanceDescription;

import de.jw.cloud42.core.domain.Instance;

/**
 * Basic functions such as listing AMIs and starting an instance.
 * 
 * @author fbitzer
 * 
 */
public class Cloud42BaseFunctions extends Cloud42Functions {

	/**
	 * List the AWS Regions.
	 * 
	 * @return
	 */
	public RegionInfo[] listRegions() {
		
		// initialize the interface
		Jec2 ec2 = this.initConnection();

		List<RegionInfo> result = new ArrayList<RegionInfo>();

		try {
			
			result = ec2.describeRegions(new ArrayList<String>());
			
			return result.toArray(new RegionInfo[0]);

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Listing regions failed: "
					+ ex.getMessage());

			//ex.printStackTrace();
			
			return null;
		}
		
	}
	
	
	
	/**
	 * Lists available EC2 AMIs that you could start an instance of.
	 * 
	 * Note that we have to work with arrays here because this class is going to
	 * be exposed as a Web service through Axis2 which does not support Lists.
	 * 
	 * @param all
	 *            set to true to display all public AMIs, set to false to list
	 *            only your own AMIs.
	 * @return Array of Images or null, if no images were found or credentials
	 *         were wrong.
	 */
	public ImageDescription[] listImages(boolean all) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();

		List<ImageDescription> result = new ArrayList<ImageDescription>();

		try {
			if (all) {
				result = ec2.describeImages(new String[] {});// .toArray(new
																// ImageDescription[0]);
			} else {
				ArrayList<String> list = new ArrayList<String>();
				list.add(this.getCredentials().getUserID());

				result = ec2.describeImagesByOwner(list);

			}

			return result.toArray(new ImageDescription[0]);

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Listing images failed: "
					+ ex.getMessage());

			//ex.printStackTrace();
			
			return null;
		}

	}

	/**
	 * Starts an instance with the given parameters.
	 * 
	 * @param imageId
	 *            AMI-Id of the instance to start.
	 * @param groups
	 *            List of groups.
	 * @param keyName
	 *            Name of the keypair to use.
	 * @param userData
	 *            User data for this instance (see
	 *            http://docs.amazonwebservices.com/AWSEC2/2008-05-05/DeveloperGuide/index.html?AESDG-chapter-instancedata.html).
	 * @param instanceType
	 *            EC2 Type of the instance
	 * @param count
	 *            number of instances to launch
	 * @param availabilityZone
	 *            Availability zone for instance.
	 * @param kernelId
	 *            KernelId to use. May be empty.
	 * @param ramdiskId
	 *            RamdiskId to use. May be empty.
	 * @return Instance or null in case of error.
	 */
	public ReservationDescription runInstance(String imageId, String[] groups,
			String keyName, byte[] userData, InstanceType instanceType,
			int count, String availabilityZone, String kernelId,
			String ramdiskId) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();

		List<String> groupList = Arrays.asList(groups);

		try {
			// ReservationDescription d = ec2.runInstances(imageId,count, count,
			// groupList, userData, keyName,instanceType);

			LaunchConfiguration l = new LaunchConfiguration(imageId);

			l.setAvailabilityZone(availabilityZone);
			l.setInstanceType(instanceType);
			l.setKernelId(kernelId);
			l.setKeyName(keyName);
			l.setMaxCount(count);
			l.setMinCount(count);
			l.setRamdiskId(ramdiskId);
			l.setSecurityGroup(groupList);
			l.setUserData(userData);

			ReservationDescription d = ec2.runInstances(l);

			return d;

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Starting instance failed: "
					+ ex.getMessage());

			return null;
		}

	}

	/**
	 * Blocking start of an instance with the given parameters. Returns only if
	 * startup of instance (or instances, of count > 1) is complete (state is
	 * "running") or if an error occured.
	 * 
	 * @param imageId
	 *            AMI-Id of the instance to start.
	 * @param groups
	 *            List of groups.
	 * @param keyName
	 *            Name of the keypair to use.
	 * @param instanceType
	 *            EC2 Type of the instance
	 * @param count
	 *            number of instances to start with in ReservationDescription
	 * @return Instance(s) (if count > 1) or null in case of error.
	 */
	public Instance[] runInstanceBlocking(String imageId, String[] groups,
			String keyName, byte[] userData, InstanceType instanceType,
			int count, String availabilityZone, String kernelId,
			String ramdiskId) {

		ReservationDescription d = runInstance(imageId, groups, keyName,
				userData, instanceType, count, availabilityZone, kernelId,
				ramdiskId);

		if (d == null)
			return null;

		List<Instance> result = new ArrayList<Instance>();

		for (com.xerox.amazonws.ec2.ReservationDescription.Instance i : d
				.getInstances()) {

			Instance inst = this.describeInstance(i.getInstanceId());

			result.add(inst);

		}

		boolean started = false;
		while (!started) {

			try {
				Thread.sleep(2500);
			} catch (Exception ex) {

			}

			// de.jw.cloud42.core.domain.Instanced =
			// describeInstance(d.getInstances().get(0).getInstanceId());
			started = true;

			for (int index = 0; index < result.size(); index++) {

				Instance i = this.describeInstance(result.get(index)
						.getInstanceId());

				result.set(index, i);

				if (!i.getState().equals("running")) {

					started = false;
				}
			}

		}
		
		// sleep a short time until SSH etc. are ready, too
		try {
			Thread.sleep(15000);
		} catch (Exception ex) {

		}

		return result.toArray(new Instance[0]);

	}

	/**
	 * Get or refresh current information on a running instance identified by
	 * its Id.
	 * 
	 * @param instanceId
	 * @return Instance holding the current properties of the instance or null
	 *         in case of error.
	 */
	public Instance describeInstance(String instanceId) {

		List<String> params = new ArrayList<String>();
		params.add(instanceId);

		// initialize the interface
		Jec2 ec2 = this.initConnection();

		try {
			List<ReservationDescription> result = ec2.describeInstances(params);

			if (result != null && result.size() == 1) {
				int index = 0;
				for (com.xerox.amazonws.ec2.ReservationDescription.Instance i : result
						.get(0).getInstances()) {

					if (i.getInstanceId().equals(instanceId)) {
						return new Instance(result.get(0), index);
					}

					index++;
				}
			}

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Describing instance failed: "
					+ ex.getMessage());

		}

		return null;
	}

	/**
	 * Stop a specific AMI instance.
	 * 
	 * @param instanceId
	 * @return TerminatingInstanceDescription or null in case of error.
	 */
	public InstanceStateChangeDescription stopInstance(String instanceId) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();

		try {

			return ec2.terminateInstances(new String[] { instanceId }).get(0);

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Terminating instance failed: "
					+ ex.getMessage());

			return null;
		}

	}

	/**
	 * Stop all existing instances.
	 * 
	 * @return List of TerminatingInstanceDescription or null in case of error
	 *         or if no instances were running.
	 */
	public TerminatingInstanceDescription[] stopAllInstances() {

		// get all existing instances and create a list of instanceIDs
		de.jw.cloud42.core.domain.Instance[] list = this.listInstances();

		if (list == null || list.length == 0)
			return null;

		List<String> instanceIds = new ArrayList<String>();
		for (de.jw.cloud42.core.domain.Instance i : list) {

			instanceIds.add(i.getInstanceId());

		}

		// initialize the interface
		Jec2 ec2 = this.initConnection();

		try {

			return ec2.terminateInstances(instanceIds).toArray(
					new TerminatingInstanceDescription[0]);

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Terminating all instances failed: "
					+ ex.getMessage());

			return null;
		}

	}

	/**
	 * Reboot a given instance.
	 * 
	 * @param instanceId
	 */
	public void rebootInstance(String instanceId) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();

		try {
			ec2.rebootInstances(new String[] { instanceId });

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Rebooting instance failed: "
					+ ex.getMessage());

		}

	}

	/**
	 * 
	 * List all instances.
	 * 
	 * @return List of AMIInstances (may be empty) or null in case of error.
	 */
	public de.jw.cloud42.core.domain.Instance[] listInstances() {

		List<String> params = new ArrayList<String>();

		// initialize the interface
		Jec2 ec2 = this.initConnection();

		try {
			List<ReservationDescription> instances = ec2
					.describeInstances(params);

			List<de.jw.cloud42.core.domain.Instance> result = new ArrayList<de.jw.cloud42.core.domain.Instance>();

			if (instances != null) {

				for (ReservationDescription d : instances) {
					for (int index = 0; index < d.getInstances().size(); index++) {
						result.add(new de.jw.cloud42.core.domain.Instance(d,
								index));
					}
				}

				return result.toArray(new de.jw.cloud42.core.domain.Instance[0]);
			}
		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Listing instances failed: "
					+ ex.getMessage());

		}
		return null;

	}

	/**
	 * Create a security group that can be assigned to an instance.
	 * 
	 * @param name
	 * @param description
	 * @return true if successfull, false else (e.g. if group with same name
	 *         already exists).
	 */
	public boolean createSecurityGroup(String name, String description) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();
		try {

			ec2.createSecurityGroup(name, description);

			return true;

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Creating security group failed: "
					+ ex.getMessage());

			return false;
		}
	}

	/**
	 * Delete a security group.
	 * 
	 * @param name
	 *            Name of the group to delete.
	 *  *
	 * @return true if successfull, false else (e.g. if group is assigned to a
	 *         running instance).
	 */
	public boolean deleteSecurityGroup(String name) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();
		try {

			ec2.deleteSecurityGroup(name);

			return true;

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Deleting security group failed: "
					+ ex.getMessage());

			return false;

		}
	}

	/**
	 * List all available Security groups.
	 * 
	 * @return
	 */
	public GroupDescription[] listSecurityGroups() {

		// initialize the interface
		Jec2 ec2 = this.initConnection();
		
		try {

			return ec2.describeSecurityGroups(new ArrayList<String>()).toArray(
					new GroupDescription[0]);

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Listing security groups failed: "
					+ ex.getMessage());

			return null;

		}
	}

	/**
	 * Gets the properties of a specific security group.
	 * 
	 * @param name
	 *            The name of the group.
	 * @return GroupDescription or null in case of error or if group does not
	 *         exist.
	 */
	public GroupDescription describeSecurityGroup(String name) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();

		try {

			return ec2.describeSecurityGroups(new String[] { name }).get(0);

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Describing security group failed: "
					+ ex.getMessage());

			return null;
		}

	}

	/**
	 * Add incoming permissions to a group by opening the given ports.
	 * 
	 * @param instance
	 * @param groupname
	 *            name of the group.
	 * @param protocol
	 *            e.g. "tcp", "udp", "icmp"
	 * @param portFrom
	 * @param portTo
	 * @param cidrIp
	 *            CIDR IP range to add (i.e. 0.0.0.0/0)
	 * 
	 * @return true if successfull, false else
	 */
	public boolean addPermission(String groupname, String protocol,
			int portFrom, int portTo, String cidrIp) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();
		
		try {

			ec2.authorizeSecurityGroupIngress(groupname, protocol, portFrom,
					portTo, cidrIp);

			return true;

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Adding incoming permissions failed: "
					+ ex.getMessage());

			return false;
		}

	}

	/**
	 * Adds a permission associated to an group/owner.
	 * 
	 * @param groupname
	 *            name of group to modify
	 * @param secGroupName
	 *            name of security group to add access
	 * @param secGroupOwnerId
	 *            owner of security group
	 * @return
	 */
	public boolean addPermission(String groupname, String secGroupName,
			String secGroupOwnerId) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();
		
		try {

			ec2.authorizeSecurityGroupIngress(groupname, secGroupName,
					secGroupOwnerId);

			return true;

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE,
					"Adding incoming permissions for source 'group' failed: "
							+ ex.getMessage());

			return false;
		}

	}

	/**
	 * Revokes permissions that were added using <code>addPermission()</code>.
	 * 
	 * @param instance
	 * @param groupname
	 * @param protocol
	 * @param portFrom
	 * @param portTo
	 * @param cidrIp
	 *            CIDR IP range to add (i.e. 0.0.0.0/0)
	 * 
	 * @return true if successfull, false else
	 */
	public boolean removePermission(String groupname, String protocol,
			int portFrom, int portTo, String cidrIp) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();
		
		try {

			ec2.revokeSecurityGroupIngress(groupname, protocol, portFrom,
					portTo, cidrIp);

			return true;

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Revoking incoming permissions failed: "
					+ ex.getMessage());

			return false;
		}

	}

	/**
	 * Revokes a permission associated to an group/owner.
	 * 
	 * @param groupname
	 *            name of group to modify
	 * @param secGroupName
	 *            name of security group to revoke access from
	 * @param secGroupOwnerId
	 *            owner of security group to revoke access from
	 * @return
	 */
	public boolean removePermission(String groupname, String secGroupName,
			String secGroupOwnerId) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();
		
		try {

			ec2.revokeSecurityGroupIngress(groupname, secGroupName,
					secGroupOwnerId);

			return true;

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE,
					"Revoking group/owner permissions failed: "
							+ ex.getMessage());

			return false;
		}

	}

	/**
	 * Register a new AMI from a bucket on S3
	 * 
	 * @param location
	 *            the location on S3
	 * @return the unique imageId of the newly registered AMI.
	 */
	public String registerImage(String location) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();
		
		try {

			return ec2.registerImage(location);

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Registering image failed: "
					+ ex.getMessage());

			return null;
		}

	}

	/**
	 * Deregister an AMI Image.
	 * 
	 * @param imageId
	 *            the ID of the image to deregister.
	 * 
	 * @return true if successfull, false else
	 */
	public boolean deregisterImage(String imageId) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();
		
		try {

			ec2.deregisterImage(imageId);

			return true;

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Deregistering image failed: "
					+ ex.getMessage());

			return false;
		}
	}

	/**
	 * Create a keypair.
	 * 
	 * @param name
	 *            Name of the keypair.
	 * @return KeyPairInfo or null in case of error.
	 */
	public KeyPairInfo createKeypair(String name) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();
		
		try {

			KeyPairInfo result = ec2.createKeyPair(name);

			return result;
		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Creating keypair failed: "
					+ ex.getMessage());

			return null;

		}
	}

	/**
	 * Delete a keypair.
	 * 
	 * @param name
	 *            Name of the keypair.
	 * 
	 * @return true if successfull, false else
	 */
	public boolean deleteKeypair(String name) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();
		
		try {

			ec2.deleteKeyPair(name);

			return true;

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Deleting keypair failed: "
					+ ex.getMessage());

			return false;
		}
	}

	/**
	 * List available keypairs.
	 * 
	 * @return List of keypairs (may be empty) or null in case of error.
	 */
	public KeyPairInfo[] listKeypairs() {
		// initialize the interface
		Jec2 ec2 = this.initConnection();

		try {

			return ec2.describeKeyPairs(new String[] {}).toArray(
					new KeyPairInfo[0]);

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Listing keypairs failed: "
					+ ex.getMessage());

			return null;
		}

	}

	/**
	 * Gets the properties of a specific keypair.
	 * 
	 * @param name
	 *            The name of the keypair.
	 * @return KeypairInfo or null in case of error or if keypair does not
	 *         exist.
	 */
	public KeyPairInfo describeKeypair(String name) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();

		try {

			return ec2.describeKeyPairs(new String[] { name }).get(0);

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Describing keypair failed: "
					+ ex.getMessage());

			return null;
		}

	}

	/**
	 * Lists available Availability zones.
	 * 
	 * @return Array of Availability zones.
	 */
	public AvailabilityZone[] listAvailabilityZones() {

		// initialize the interface
		Jec2 ec2 = this.initConnection();

		try {
			List<String> l = new ArrayList<String>();
			return ec2.describeAvailabilityZones(l).toArray(
					new AvailabilityZone[0]);

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Listing availability zones failed: "
					+ ex.getMessage());

			return null;
		}

	}

	/**
	 * Get console ouput for a particular instance.
	 * 
	 * @param instanceId
	 *            The instanceId of the instance to get the console output from.
	 * @return ConsoleOutput object containing output information or null in
	 *         case of error.
	 */
	public ConsoleOutput getConsoleOutput(String instanceId) {

		// initialize the interface
		Jec2 ec2 = this.initConnection();

		try {

			return ec2.getConsoleOutput(instanceId);

		} catch (Exception ex) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Retrieving console output for instance "
					+ instanceId + " failed: " + ex.getMessage());

			return null;
		}

	}
	
	
	
	
	
	
	
	private Jec2 initConnection(){
		
		try {
			
			Cloud42Settings config = Cloud42Settings.getInstance("config.properties");

			boolean isSecure = config.getBoolean("useHTTPS");
			
			// initialize the interface
			Jec2 ec2 = new Jec2(getCredentials().getAwsAccessKeyId(),
					getCredentials().getSecretAccessKey(), isSecure, config.getString("server"),
					config.getInteger("port"));
			
			
			ec2.setResourcePrefix(config.getString("resourcePrefix"));
			
			ec2.setSignatureVersion(config.getInteger("signatureVersion"));
		
			//set region
			if (this.getRegionUrl() != null && !this.getRegionUrl().equals("")){
				ec2.setRegionUrl(getRegionUrl());
			}
			
			return ec2;
			
		} catch (Exception ex) {

			Logger.getAnonymousLogger().severe(
					"Initializing configuration failed.");

			ex.printStackTrace();
			
			return null;
		}
	}

}
