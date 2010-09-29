/**
 * 
 */
package de.jw.cloud42.webapp;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Simple backing bean for creating a new permission. Holds the values entered in the UI.
 * Also provides default values for a new permission.
 * 
 * @author fbitzer
 *
 */
@Name("permissionConfiguration")
@Scope(ScopeType.SESSION)
public class PermissionConfiguration {
	
	/**
	 * The group for which the permissionConfiguration applies
	 */
	private String editedGroup;
	
	private String cidr = "0.0.0.0/0";
	
	private String source = "cidr";
	
	private String protocol = "tcp";
	
	//must be Integer so that it is nullable (for validation in the UI)
	private Integer fromPort = 1;
	private Integer toPort = 65535;
	
	/**
	 * the source user id
	 */
	private String user;
	
	private String group;

	/**
	 * @return the cidr
	 */
	public String getCidr() {
		return cidr;
	}

	/**
	 * @param cidr the cidr to set
	 */
	public void setCidr(String cidr) {
		this.cidr = cidr;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return the fromPort
	 */
	public Integer getFromPort() {
		return fromPort;
	}

	/**
	 * @param fromPort the fromPort to set
	 */
	public void setFromPort(Integer fromPort) {
		this.fromPort = fromPort;
	}

	/**
	 * @return the toPort
	 */
	public Integer getToPort() {
		return toPort;
	}

	/**
	 * @param toPort the toPort to set
	 */
	public void setToPort(Integer toPort) {
		this.toPort = toPort;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @param group the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * @return the editedGroup
	 */
	public String getEditedGroup() {
		return editedGroup;
	}

	/**
	 * @param editedGroup the editedGroup to set
	 */
	public void setEditedGroup(String editedGroup) {
		this.editedGroup = editedGroup;
	}
	
	

}
