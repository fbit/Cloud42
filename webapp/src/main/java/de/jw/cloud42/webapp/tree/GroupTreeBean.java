/**
 * 
 */
package de.jw.cloud42.webapp.tree;



import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.richfaces.component.UITree;
import org.richfaces.event.NodeSelectedEvent;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

import com.xerox.amazonws.ec2.GroupDescription;
import com.xerox.amazonws.ec2.GroupDescription.IpPermission;

import de.jw.cloud42.webapp.BaseFunctionsManager;


/**
 * 
 * Backing bean for the TreeView on the "Security groups" screen.
 * Creates a hierarchical order between groups and their permissions.
 * Uses <code>GropTreeNode</code> and <code>PermissionTreeNode</code> for the nodes of the tree.
 * 
 * @author Frank
 *
 */
@Name("groupTreeBean")
public class GroupTreeBean {
	
		/**
		 * Inject baseFunctionsManager to retrieve groups and permissions from the API.
		 */
		@In
		BaseFunctionsManager baseFunctionsManager;
	
		
	    private TreeNode rootNode = null;
	    
	    /**
	     * Holds the currently selected permission, if any
	     */
	    private IpPermission selectedPermission;
	   
	    /**
	     * Holds the currently selected group, if any
	     */
	    private GroupDescription selectedGroup;
	    
	   
	    /**
	     * Adds permission nodes to a group node.
	     * 
	     * @param groupNode the root group node.
	     * @param group the grop to add permissions for.
	     */
	    private void addPermissionNodes(TreeNode<GroupDescription> groupNode, GroupDescription group) {
	
	    	
	    	int count = 0;
	    	for (IpPermission p : group.getPermissions()){
	    	
	    		TreeNodeImpl nodeImpl = new TreeNodeImpl();
                nodeImpl.setData(new PermissionTreeNode(group, p));
                
                
                groupNode.addChild(new Integer(count), nodeImpl);
	    		
                count++;
	    	}
	    	
	    	//if no permissions are available, set a dummy node
	    	if (count == 0){
	    		TreeNodeImpl nodeImpl = new TreeNodeImpl();
                nodeImpl.setData("(no permissions set)");
                
                
                groupNode.addChild(new Integer(count), nodeImpl);
	    	}
	    	
	    }
	    
	    /**
	     * Entry point, creates the tree of groups and permissions.
	     */
	    private void loadTree() {
	        
	        
	            rootNode = new TreeNodeImpl();
	            
	            List<GroupDescription> groups = baseFunctionsManager.getGroups();
	            int count = 0;
	            if (groups != null) {
		            for (GroupDescription d : groups){
		            
		            	TreeNodeImpl groupNode = new TreeNodeImpl();
		            	
		            	groupNode.setData(new GroupTreeNode(d));
		            	
		            	
		            	
		            	rootNode.addChild(count, groupNode);
		            	
		            	addPermissionNodes(groupNode, d);
		            
		            	count ++;
		            }
	        
	    		}
	    }
	    
	    /**
	     * Property for value binding in UI. Causes loading of the tree if neccessary.
	     * 
	     * @return
	     */
	    public TreeNode getTreeNode() {
	        if (rootNode == null) {
	            loadTree();
	        }
	        
	        return rootNode;
	    }
	    
	    
	    /**
	     * Reset the tree and force a reload next time it is accessed.
	     */
	    public void resetGroupList(){
	    	
	    	rootNode = null;
	    	
	    }
	    

		/**
		 * Action that is execueted when a node is selected. Sets the currently selected group and permission.
		 * @param event
		 */
	    public void processSelection(NodeSelectedEvent event) {
	    try {
	   
	    	UITree tree = (UITree) event.getComponent();
	        
	        Object v = tree.getRowData();
	        
	        if (v instanceof GroupTreeNode){
	        	this.selectedGroup = ((GroupTreeNode)v).getGroupDescription();
	        	this.selectedPermission = null;
	        } else if (v instanceof PermissionTreeNode){
	        	this.selectedGroup = ((PermissionTreeNode)v).getGroup();
	        	this.selectedPermission = ((PermissionTreeNode)v).getIpPermission();
	        }

	    } catch(Exception ex){
	    	this.selectedGroup=null;
	    	this.selectedPermission=null;
	    }
	    }

	    
	    
		/**
		 * @return the selectedPermission
		 */
		public IpPermission getSelectedPermission() {
			return selectedPermission;
		}

		/**
		 * @return the selectedGroup
		 */
		public GroupDescription getSelectedGroup() {
			return selectedGroup;
		}
	    
	    
	}

