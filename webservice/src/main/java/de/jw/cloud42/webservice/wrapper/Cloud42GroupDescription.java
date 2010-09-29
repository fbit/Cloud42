/**
 * 
 */
package de.jw.cloud42.webservice.wrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Extends Typicas GroupDescription and adds list of permissions as an array of
 * {@link Cloud42IpPermission} since Axis2 does not recognize the standard list of the nested type
 * <code>GroupDescription.IpPermission</code>.
 * 
 * @author fbitzer
 *
 */
public class Cloud42GroupDescription extends com.xerox.amazonws.ec2.GroupDescription{

	public Cloud42GroupDescription(String name, String desc, String owner){
		super( name,  desc, owner);
	}
	
	
	/**
	 * Load the properties of the given "original" GroupDescription in this instance.
	 * 
	 * @param gd
	 */
	public static Cloud42GroupDescription parse(com.xerox.amazonws.ec2.GroupDescription gd){
		
		Cloud42GroupDescription result = new Cloud42GroupDescription(gd.getName(),gd.getDescription(), gd.getOwner());
		
		List<IpPermission> permissions = gd.getPermissions();
		
		for (IpPermission p : permissions){
			
			result.addPermission(p.getProtocol(), p.getFromPort(), p.getToPort());
			
		}
		
		return result;
		
	}
	
	/**
	 * Return current permissions as array of type Cloud42IpPermission.
	 * Reason: Typica's GroupDescription uses a List of the nested type GroupDescription.IpPermission
	 * and Axis2 does not recognize and publish this type, so it is not accessible in client applications
	 * unless you include Typica.
	 
	 * @return
	 */
	 public Cloud42IpPermission[] getPermissionsAsArray() {
		 
         List<IpPermission> pList = super.getPermissions();
         
         List<Cloud42IpPermission> cloud42List = new ArrayList<Cloud42IpPermission>();
         
         for (IpPermission p : pList){
        	 cloud42List.add(new Cloud42IpPermission(p));
         }
         
         return cloud42List.toArray(new Cloud42IpPermission[0]);
         
 }

	
}
