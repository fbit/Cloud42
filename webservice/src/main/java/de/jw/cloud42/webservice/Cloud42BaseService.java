/**
 * 
 */
package de.jw.cloud42.webservice;

import java.util.ArrayList;
import java.util.List;

//import com.xerox.amazonws.ec2.GroupDescription;
import com.xerox.amazonws.ec2.AvailabilityZone;
import com.xerox.amazonws.ec2.ConsoleOutput;
import com.xerox.amazonws.ec2.ImageDescription;
import com.xerox.amazonws.ec2.InstanceStateChangeDescription;
import com.xerox.amazonws.ec2.InstanceType;
import com.xerox.amazonws.ec2.KeyPairInfo;
import com.xerox.amazonws.ec2.RegionInfo;
import com.xerox.amazonws.ec2.TerminatingInstanceDescription;
//import com.xerox.amazonws.ec2.ReservationDescription.Instance;

import de.jw.cloud42.core.domain.AwsCredentials;
import de.jw.cloud42.core.domain.Instance;
import de.jw.cloud42.core.service.Cloud42BaseFunctions;
import de.jw.cloud42.webservice.wrapper.Cloud42GroupDescription;

/**
 * Wrapper around {@link de.jw.cloud42.core.service.Cloud42BaseFunctions} to provide the base functions as 
 * a stateless web service. See there for a more detailed method reference.
 * 
 * @author fbitzer
 *
 */
public class Cloud42BaseService {
	
		/**
		 * Wraps {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#listRegions()}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#listRegions()
		 */
		public RegionInfo[] listRegions(AwsCredentials credentials){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(null);
			
			return f.listRegions();
			
		}
	
	
		/**
		 * Wraps {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#listImages(boolean)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#listImages(boolean)
		 *
		 */
		public ImageDescription[] listImages(AwsCredentials credentials, String regionUrl, boolean all){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			return f.listImages(all);
			
		}
		

		/**
		 * Wraps {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#runInstance(java.lang.String imageId, 
		 	java.lang.String[] groups, 
			java.lang.String keyName, byte[] userData, com.xerox.amazonws.ec2.InstanceType instanceType, 
			int count,
			java.lang.String availabilityZone, java.lang.String kernelId, java.lang.String ramdiskId)}
		 *	
		 * <br/>
		 * Parameter instanceType is passed as String for compatibility reasons.
		 * The official EC2 types are supported. 
		 * See http://www.amazon.com/Instances-EC2-AWS/b?ie=UTF8&node=370375011.
		 * 
		 * @param regionUrl is the URL of the AWS region to use as String, e.g. "ec2.eu-west-1.amazonaws.com"
		 * @param instanceType possible values are m1.small, m1.large, m1.xlarge, c1.medium, c1.xlarge
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#runInstance(java.lang.String imageId, 
		 	java.lang.String[] groups, 
			java.lang.String keyName, byte[] userData, com.xerox.amazonws.ec2.InstanceType instanceType, 
			int count,
			java.lang.String availabilityZone, java.lang.String kernelId, java.lang.String ramdiskId)
		 */
		public Instance[] runInstance(AwsCredentials credentials, String regionUrl, String imageId, String[] groups,String keyName, 
				String userData, String instanceType, int count, String availabilityZone, String kernelId,
				String ramdiskId){
			
			return this.runInstanceWithBinary(credentials, regionUrl, imageId, groups, keyName, 
					userData.getBytes(), instanceType, count, availabilityZone, kernelId, ramdiskId);
		}
		
		
		/**
		 * Wraps {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#runInstance(java.lang.String imageId, 
		 	java.lang.String[] groups, 
			java.lang.String keyName, byte[] userData, com.xerox.amazonws.ec2.InstanceType instanceType, 
			int count,
			java.lang.String availabilityZone, java.lang.String kernelId, java.lang.String ramdiskId)}
		 *	
		 * <br/>
		 * <b>Provides possibility to attach binary data at instance startup.</b>
		 * <br/>
		 * Parameter instanceType is passed as String for compatibility reasons.
		 * The official EC2 types are supported. See 
		 * http://www.amazon.com/Instances-EC2-AWS/b?ie=UTF8&node=370375011.
		 * 
		 * @param instanceType possible values are m1.small, m1.large, m1.xlarge, c1.medium, c1.xlarge
		 * @param binaryUserData Binary data to attach to instance (watch for limited file size!).
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#runInstance(java.lang.String imageId, 
		 	java.lang.String[] groups, 
			java.lang.String keyName, byte[] userData, com.xerox.amazonws.ec2.InstanceType instanceType, 
			int count,
			java.lang.String availabilityZone, java.lang.String kernelId, java.lang.String ramdiskId)
		 */
		public Instance[] runInstanceWithBinary(AwsCredentials credentials, String regionUrl, String imageId, String[] groups,String keyName, 
				byte[] binaryUserData, String instanceType, int count, String availabilityZone, String kernelId,
				String ramdiskId){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			InstanceType type = InstanceType.getTypeFromString(instanceType);
			
			com.xerox.amazonws.ec2.ReservationDescription d = 
				f.runInstance(imageId, groups, keyName, binaryUserData, 
								type,count,availabilityZone,kernelId,ramdiskId);
			
			List<Instance> result = new ArrayList<Instance>();
			if (d!=null){
				
				
				for (int index = 0; index < d.getInstances().size(); index++){
					result.add(new Instance(d, index));
				}
			} else {
				return null;
			}
			
			return result.toArray(new Instance[0]);
		}
		
		/**
		 * Wraps {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#runInstanceBlocking(java.lang.String imageId, 
		 	java.lang.String[] groups, 
			java.lang.String keyName, byte[] userData, com.xerox.amazonws.ec2.InstanceType instanceType, 
			int count,
			java.lang.String availabilityZone, java.lang.String kernelId, java.lang.String ramdiskId)}
		 *	
		 * <br/>
		 * <b>Provides possibility to attach binary data at instance startup.</b>
		 * <br/>
		 * Parameter instanceType is passed as String for compatibility reasons.
		 * The official EC2 types are supported. See 
		 * http://www.amazon.com/Instances-EC2-AWS/b?ie=UTF8&node=370375011.
		 * 
		 * @param instanceType possible values are m1.small, m1.large, m1.xlarge, c1.medium, c1.xlarge
		 * @param binaryUserData Binary data to attach to instance (watch for limited file size!).
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#runInstanceBlocking(java.lang.String imageId, 
		 	java.lang.String[] groups, 
			java.lang.String keyName, byte[] userData, com.xerox.amazonws.ec2.InstanceType instanceType, 
			int count,
			java.lang.String availabilityZone, java.lang.String kernelId, java.lang.String ramdiskId)
		 */
		public Instance[] runInstanceBlockingWithBinary(AwsCredentials credentials, String regionUrl, String imageId, String[] groups,String keyName, 
				byte[] binaryUserData, String instanceType, int count, String availabilityZone, String kernelId,
				String ramdiskId){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			InstanceType type = InstanceType.getTypeFromString(instanceType);
			
			
			return f.runInstanceBlocking(imageId, groups, keyName,
					binaryUserData, type, count, availabilityZone,kernelId,ramdiskId);
		}
		
		
		/**
		 * Wraps {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#runInstanceBlocking(java.lang.String imageId, 
		 	java.lang.String[] groups, 
			java.lang.String keyName, byte[] userData, com.xerox.amazonws.ec2.InstanceType instanceType, 
			int count,
			java.lang.String availabilityZone, java.lang.String kernelId, java.lang.String ramdiskId)}
		 *	
		 * <br/>
		 * Parameter instanceType is passed as String for compatibility reasons.
		 * The official EC2 types are supported. See 
		 * http://www.amazon.com/Instances-EC2-AWS/b?ie=UTF8&node=370375011.
		 * 
		 * @param regionUrl is the URL of the AWS region to use as String, e.g. "ec2.eu-west-1.amazonaws.com"
		 * @param instanceType possible values are m1.small, m1.large, m1.xlarge, c1.medium, c1.xlarge
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#runInstanceBlocking(java.lang.String imageId, 
		 	java.lang.String[] groups, 
			java.lang.String keyName, byte[] userData, com.xerox.amazonws.ec2.InstanceType instanceType, 
			int count,
			java.lang.String availabilityZone, java.lang.String kernelId, java.lang.String ramdiskId)
		 */
		public Instance[] runInstanceBlocking(AwsCredentials credentials, String regionUrl, String imageId, String[] groups,String keyName,
				String userData, String instanceType, int count, String availabilityZone, String kernelId,
				String ramdiskId){
			
			return this.runInstanceBlockingWithBinary(credentials, regionUrl, imageId, groups,
					keyName, userData.getBytes(), instanceType, count, availabilityZone, kernelId, ramdiskId);
		}
		
		
		
		
		/**
		 * Wraps {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#describeInstance(java.lang.String)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#describeInstance(java.lang.String)
		 *
		 */
		public Instance describeInstance(AwsCredentials credentials, String regionUrl, String instanceId){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			return  f.describeInstance(instanceId);
			
		}
		

		
		/**
		 * Wraps {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#stopInstance(java.lang.String)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#stopInstance(java.lang.String)
		 *
		 */
		public InstanceStateChangeDescription stopInstance(AwsCredentials credentials, String regionUrl, String instanceId){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			return f.stopInstance(instanceId);
			
			
		}
		
		/**
		 * Wraps {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#stopAllInstances()}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#stopAllInstances()
		 *
		 */
		public TerminatingInstanceDescription[] stopAllInstances(AwsCredentials credentials, String regionUrl){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			return f.stopAllInstances();
			
		}
		
		/**
		 * Wraps {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#rebootInstance(java.lang.String)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#rebootInstance(java.lang.String)
		 *
		 */
		public void rebootInstance(AwsCredentials credentials, String regionUrl, String instanceId){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			f.rebootInstance(instanceId);
				
		}
		
		
		/**
		 * Wraps {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#listInstances()}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#listInstances()
		 *
		 */
		public Instance[] listInstances(AwsCredentials credentials, String regionUrl){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			return f.listInstances();
			
		}
		
		/**
		 * Wraps 
		 * {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#createSecurityGroup(java.lang.String, java.lang.String)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#createSecurityGroup(java.lang.String, java.lang.String)
		 *
		 */
		public boolean createSecurityGroup(AwsCredentials credentials, String regionUrl, String name, String description){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			return f.createSecurityGroup(name, description);
		}
		
		
		/**
		 * Wraps 
		 * {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#deleteSecurityGroup(java.lang.String)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#deleteSecurityGroup(java.lang.String)
		 *
		 */
		public boolean deleteSecurityGroup(AwsCredentials credentials, String regionUrl, String name){
			
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			return f.deleteSecurityGroup(name);
		}
		
		
		/**
		 * Wraps 
		 * {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#listSecurityGroups()}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#listSecurityGroups()
		 *
		 */
		public Cloud42GroupDescription[] listSecurityGroups(AwsCredentials credentials, String regionUrl){
			
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			
			com.xerox.amazonws.ec2.GroupDescription[] g = f.listSecurityGroups();
			if (g!=null){
				
				List<Cloud42GroupDescription> result = new ArrayList<Cloud42GroupDescription>();
				
				for (com.xerox.amazonws.ec2.GroupDescription d : g){
					result.add(Cloud42GroupDescription.parse(d));
				}
				
				return result.toArray(new Cloud42GroupDescription[0]);
				
			} else {
				return null;
			}
			
			
			
		}
		
		
		/**
		 * Wraps 
		 * {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#describeSecurityGroup(java.lang.String)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#describeSecurityGroup(java.lang.String)
		 *
		 */
		public Cloud42GroupDescription describeSecurityGroup(AwsCredentials credentials, String regionUrl, String name){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			com.xerox.amazonws.ec2.GroupDescription d = f.describeSecurityGroup(name);
			
			if (d!=null){
				return Cloud42GroupDescription.parse(d);
			} else {
				return null;
			}
		}
		
		
		/**
		 * Wraps 
		 * {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#addPermission(java.lang.String, java.lang.String, 
		 * int, int, java.lang.String)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#addPermission(java.lang.String, java.lang.String, 
		 * int, int, java.lang.String)
		 *
		 */
		public boolean addPermission(AwsCredentials credentials, String regionUrl, String groupname, 
				String protocol, int portFrom, int portTo,  String cidrIp){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			
			f.setRegionUrl(regionUrl);
			
			return f.addPermission(groupname, protocol, portFrom, portTo,cidrIp);
			
		}
		
		/**
		 * Wraps 
		 * {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#addPermission(java.lang.String, java.lang.String, 
		 * java.lang.String)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#addPermission(java.lang.String, java.lang.String, 
		 * java.lang.String)
		 *
		 */
		public boolean addPermissionSecGroup(AwsCredentials credentials, String regionUrl, String groupname, 
				String secGroupName, String secGroupOwnerId){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			
			f.setRegionUrl(regionUrl);
			
			return f.addPermission(groupname,secGroupName,secGroupOwnerId);
			
		}
		
			
		
		/**
		 * Wraps 
		 * {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#removePermission(java.lang.String, java.lang.String, 
		 * int, int, java.lang.String)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#removePermission(java.lang.String, java.lang.String, 
		 * int, int, java.lang.String)
		 *
		 */
		public boolean removePermission(AwsCredentials credentials, String regionUrl, String groupname, 
				String protocol, int portFrom, int portTo, String cidrIp){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			return f.removePermission(groupname, protocol, portFrom, portTo, cidrIp);
			
		}
		
		/**
		 * Wraps 
		 * {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#removePermission(java.lang.String, java.lang.String, 
		 *  java.lang.String)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#removePermission(java.lang.String, java.lang.String, 
		 *  java.lang.String)
		 *
		 */
		public boolean removePermissionSecGroup(AwsCredentials credentials, String regionUrl, String groupname, 
				  String secGroupName,
                  String secGroupOwnerId){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			return f.removePermission(groupname, secGroupName, secGroupOwnerId);
			
		}
		
		
		
		
		/**
		 * Wraps 
		 * {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#registerImage(java.lang.String)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#registerImage(java.lang.String)
		 *
		 */
		public String registerImage(AwsCredentials credentials, String regionUrl, String location){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			return f.registerImage(location);
			
		}
		
		/**
		 * Wraps 
		 * {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#deregisterImage(java.lang.String)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#deregisterImage(java.lang.String)
		 *
		 */
		public boolean deregisterImage(AwsCredentials credentials, String regionUrl, String imageId){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			return f.deregisterImage(imageId);
		}
		
		/**
		 * Wraps 
		 * {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#createKeypair(java.lang.String)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#createKeypair(java.lang.String)
		 *
		 */
		public KeyPairInfo createKeypair(AwsCredentials credentials, String regionUrl, String name){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			return f.createKeypair(name);
			
		}
		
		/**
		 * Wraps 
		 * {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#deleteKeypair(java.lang.String)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#deleteKeypair(java.lang.String)
		 *
		 */
		public boolean deleteKeypair(AwsCredentials credentials, String regionUrl, String name){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			return f.deleteKeypair(name);
			
		}
		
		/**
		 * 
		 * Wraps {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#listKeypairs()}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#listKeypairs()
		 * 
		 */
		public KeyPairInfo[] listKeypairs(AwsCredentials credentials, String regionUrl){
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			return f.listKeypairs();
		
		}
		
		/**
		 * Wraps 
		 * {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#describeKeypair(java.lang.String)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#describeKeypair(java.lang.String)
		 *
		 */
		public KeyPairInfo describeKeypair(AwsCredentials credentials, String regionUrl, String name){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			return f.describeKeypair(name);
			
		}
		
		
		/**
		 * 
		 * Wraps {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#listAvailabilityZones()}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#listAvailabilityZones()
		 * 
		 */
		public AvailabilityZone[] listAvailabilityZones(AwsCredentials credentials, String regionUrl){
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			return f.listAvailabilityZones();
		
		}
		
		
		/**
		 * Wraps 
		 * {@link de.jw.cloud42.core.service.Cloud42BaseFunctions#getConsoleOutput(java.lang.String)}.
		 * 
		 * @see de.jw.cloud42.core.service.Cloud42BaseFunctions#getConsoleOutput(java.lang.String)
		 *
		 * @return a String value containing the console output.
		 */
		public String getConsoleOutput(AwsCredentials credentials, String regionUrl, String instanceId){
			
			Cloud42BaseFunctions f = new Cloud42BaseFunctions();
			f.setCredentials(credentials);
			f.setRegionUrl(regionUrl);
			
			ConsoleOutput o = f.getConsoleOutput(instanceId);
			
			if (o != null){
				String s = o.getOutput();
				//we have to parse the output string since it may contain illegal escape sequences
				//that can not be transferred using XML
				byte[] outputBytes =  s.getBytes();
				
				for (int i=0; i < outputBytes.length; i++){
					if (outputBytes[i] == 0x1b ||outputBytes[i] == 0x8){
						outputBytes[i] = 0x20; //replace the invalid sequences by a space character
					}
				}
				
				String res = new String(outputBytes);
				
				return res;
				
				
			} else {
				return null;
			}
		}
			
		
		
		

}
