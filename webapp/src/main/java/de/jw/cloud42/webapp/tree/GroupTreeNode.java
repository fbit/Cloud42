/**
 * 
 */
package de.jw.cloud42.webapp.tree;

import com.xerox.amazonws.ec2.GroupDescription;

/**
 * Represents a tree node holding a GroupDescription object.
 * Returns a proper toString message.
 * 
 * @author Frank
 *
 */
public class GroupTreeNode {
	
	
	
	/**
	 * The actual GroupDescription.
	 */
	private GroupDescription groupDescription;
	
	
	
	/**
	 * Use constructor to pass represented GroupDescription.
	 * @param forGroup
	 */
	public GroupTreeNode(GroupDescription forGroup){
		groupDescription = forGroup;
	}

	public GroupDescription getGroupDescription() {
		return groupDescription;
	}
	

	/**
	 * Return group details in format <i>{groupName} ({groupDescription}, owned by {owner})</i>.
	 */
	public String toString(){
		return groupDescription.getName() +
			" (" + groupDescription.getDescription() + ", owned by " + 
			groupDescription.getOwner() + ")";
	}
	

}
