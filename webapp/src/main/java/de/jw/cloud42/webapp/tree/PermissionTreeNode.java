/**
 * 
 */
package de.jw.cloud42.webapp.tree;

import com.xerox.amazonws.ec2.GroupDescription;
import com.xerox.amazonws.ec2.GroupDescription.IpPermission;

/**
 * 
 * Represents a tree node holding a IpPermission object.
 * Returns a proper toString message for the associated permission.
 * 
 * 
 * @author Frank
 *
 */
public class PermissionTreeNode {
	
		
	
	/**
	 * The actual IpPermission.
	 */
	private IpPermission ipPermission;
	
	/**
	 * The group that the actual permission belongs to
	 */
	private GroupDescription group;
	
	
	/**
	 * @return the group
	 */
	public GroupDescription getGroup() {
		return group;
	}

	/**
	 * Use constructor to pass represented IpPermission.
	 * @param forGroup
	 */
	public PermissionTreeNode(GroupDescription group, IpPermission forPermission){
		this.group = group;
		ipPermission = forPermission;
	}

	public IpPermission getIpPermission() {
		return ipPermission;
	}
	

	/**
	 * Return permission details as readable string.
	 */
	public String toString(){
		
		String result = ipPermission.getProtocol().toUpperCase() + ": from port " + 
		ipPermission.getFromPort() + " to " + ipPermission.getToPort();
		
		//check source mode
		if (ipPermission.getIpRanges() != null && ipPermission.getIpRanges().size()>0){
			String cidranges = "";
			for (String cidr : ipPermission.getIpRanges()){
				cidranges = cidranges  + cidr + ", ";
			}
				
			result = result + " (Source CIDR " + cidranges.substring(0, cidranges.length()-2) + ")";
			
		} else {
			//Group Source mode
			String pairs = "";
			for (String[] pair : ipPermission.getUidGroupPairs()){
				pairs = pairs + pair[0] + ":" + pair[1] + ", ";
				
			}
			result = result + " (Source (User:Group) " + pairs.substring(0, pairs.length()-2) + ")";
				
		}
		
		
		return result;
		
	}
	
	
	
	
}
