/**
 * 
 */
package de.jw.cloud42.webservice.wrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around Typicas GroupDesritopm.IpPermission since Axis2 does not recognize nested types.
 * Morever, cidrIps is returned as array instead of list.
 * 
 * @author fbitzer
 *
 */
public class Cloud42IpPermission {
	
	private String protocol;
    private int fromPort;
    private int toPort;
    private List<String> cidrIps = new ArrayList<String>();
    
    //not used!
    //private List<String[]> uid_group_pairs = new ArrayList<String[]>();

	
    /**
     * Load data from wrapped object in the constructor.
     */
	public Cloud42IpPermission(com.xerox.amazonws.ec2.GroupDescription.IpPermission p) {
		this.protocol = p.getProtocol();
		this.fromPort = p.getFromPort();
		this.toPort = p.getToPort();
		this.cidrIps = p.getIpRanges();
		
	}
	
	
	
	public String getProtocol() {
        return protocol;
	}

	public int getFromPort() {
	        return fromPort;
	}
	
	public int getToPort() {
	        return toPort;
	}
	
	public void addIpRange(String cidrIp) {
	        this.cidrIps.add(cidrIp);
	}
	
	public String[] getIpRanges() {
	        return cidrIps.toArray(new String[0]);
	}




}
