package de.jw.cloud42.core.service;


import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.xerox.amazonws.ec2.GroupDescription;
import com.xerox.amazonws.ec2.ImageDescription;
import com.xerox.amazonws.ec2.InstanceType;
import com.xerox.amazonws.ec2.KeyPairInfo;
import com.xerox.amazonws.ec2.ReservationDescription;




import de.jw.cloud42.core.domain.AwsCredentials;
import de.jw.cloud42.core.domain.Instance;
import de.jw.cloud42.core.service.Cloud42BaseFunctions;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for Cloud42BaseFunctions service class.
 * 
 * NOTE: testControlInstances should be uncommented during usual build process since it starts and stops
 * real EC2 instances which cost real money...
 */
public class Cloud42BaseFunctionsTest 
    extends TestCase
{
	
	Cloud42BaseFunctions bf; 
	boolean runAll = false;
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public Cloud42BaseFunctionsTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( Cloud42BaseFunctionsTest.class );
    }
    
    /**
     * Setup, create a BaseFunctions object with the credentials.
     */
    protected void setUp() {
    	
    	bf = new Cloud42BaseFunctions();
    	
    	try {
    		
    		Configuration config = new PropertiesConfiguration("test-config.properties");

    		AwsCredentials cred = new AwsCredentials();
    		
    		cred.setAwsAccessKeyId(config.getString("AWSKey"));
    		cred.setSecretAccessKey(config.getString("SecretAccessKey"));
    		cred.setUserID(config.getString("Username"));
    		
    		bf.setCredentials(cred);
    		//run tests in EU region!
    		bf.setRegionUrl("ec2.eu-west-1.amazonaws.com");
    		
    		runAll = config.getBoolean("runFullTests");
    		
    	} catch (Exception ex) {
    		
    		Logger.getAnonymousLogger().severe("Reading test configuration failed.");
    		
    		assert(false);
    	}
      }
    
    /**
     * Stop all instances after test.
     */
    protected void tearDown() {
    	
    	if (bf!=null && runAll){
    		bf.stopAllInstances();
    	}
    	
    }

    
    
    /**
     * Test listImages
     */
    public void testListImages()
    {
       
        
        ImageDescription[] l = bf.listImages(true);
        
        assert(Arrays.asList(l).size()>0);
        
        
    }
    
    
    /**
     * Test starting, rebooting, stopping, listing instances.
     * 
     * WARNING: This test should be used with care since it has side effects on
     * other running EC2 instances. Moreover, it causes costs, because it starts up
     * to 4 EC2 AMI instances.
     * Because of this, this test case is only executed when runFullTests is set to True in the
     * configuration file.
     * 
     */
     public void testControlInstance()
    {
    	 
    	 //only execute this test if flag is set
    	if (!runAll) {
    		Logger.getAnonymousLogger().info("Testcase 'testControlInstance' was skipped because " +
    				"it was told to do so in the configuration file.");
    		return;
    	}
    	
   	 
        //at first make sure there is no instance running.
        bf.stopAllInstances();
        
        Instance[] il;
        
        il = bf.listInstances();
        for (Instance i: il){
        	assert (i.getState().equals("shutting-down") ||
        			i.getState().equals("terminated"));
        	
        }
        
       
        ImageDescription[] l = bf.listImages(false);
        
        //if there is no image for the current user, skip the whole test
        if (l.length == 0){
        	Logger.getAnonymousLogger().info("Testcase 'testControlInstance' was skipped because no " +
        			"AMI could be found for the current user account.");
        	return;
        }
        
        assert(l.length>0);
        
        KeyPairInfo k[] = bf.listKeypairs();
        
        assert(Arrays.asList(k).size()>0);
        
        //pick first image and first keypair and start instance
        ReservationDescription d = bf.runInstance(l[0].getImageId(), new String[]{"default"}, 
        		k[0].getKeyName(), null, InstanceType.DEFAULT, 1 , "", "", "");
        
        il = bf.listInstances();
        
        //started instance was added to the list.
        boolean found = false;
        for (Instance i: il){
        	if (i.getInstanceId().equals(d.getInstances().get(0).getInstanceId())){
        		found = true;
        	}
        }
        assert (found);
        
        
        //start a second instance form same image
        //use blocking call
        Instance[] i2 = bf.runInstanceBlocking(l[0].getImageId(), new String[]{"default"}, 
        		k[0].getKeyName(),null, InstanceType.DEFAULT, 2, "", "", "");
        
        
        //it was a blocking run, so instance must be there as soon as method returns
        
        assert (i2[0].isRunning());
        assert (i2[1].isRunning());
        
        
        assert (bf.describeInstance(i2[0].getInstanceId()).isRunning());
        assert (bf.describeInstance(i2[1].getInstanceId()).isRunning());
        
    	 
        il = bf.listInstances();
        
        
        //i2 has to be in the list
        found = false;
        for (Instance i: il){
        	if (i.getInstanceId().equals(i2[0].getInstanceId())){
        		found = true;
        	}
        }
        assert (found);
        
        //now reboot instance 2
        bf.rebootInstance(i2[0].getInstanceId());
        
        
        
        //start a third instance
        com.xerox.amazonws.ec2.ReservationDescription.Instance i3 = bf.runInstance(l[0].getImageId(),
        		new String[]{"default"}, k[0].getKeyName(), null, 
        		InstanceType.DEFAULT,1, "", "" ,"").getInstances().get(0);
        
        il = bf.listInstances();
        
        //i3 has to be in the list
        found = false;
        for (Instance i: il){
        	if (i.getInstanceId().equals(i3.getInstanceId())){
        		found = true;
        	}
        }
        assert (found);
        
      
        
        //shutdown instance 2
        bf.stopInstance(i2[0].getInstanceId());
        
        il = bf.listInstances();
        
        //state must be shutting-down
        found = false;
        for (Instance i: il){
        	if (i.getInstanceId().equals(i2[0].getInstanceId())){
        		found = true;
        		assert (i.getState().equals("shutting-down"));
        	} 
        }
        assert (found);
        
        
        
        //shutdown all other instances
        bf.stopAllInstances();
        try {
        	Thread.sleep(1000);
        }catch (Exception ex){
        	
        }
        
        il = bf.listInstances();
        for (Instance i: il){
        	assert (i.getState().equals("shutting-down") ||
        			i.getState().equals("terminated"));
        	
        }
        
        
        
   }
    
    /**
     * Test creating and deleting a keypair.
     */
    public void testKeypairs()
    {
        
        KeyPairInfo[] kl = bf.listKeypairs();
        //save number of existing keypairs before test
        int oldSize = Arrays.asList(kl).size();
        
        //create keypair
        KeyPairInfo k = bf.createKeypair("Cloud42Testpair");
        
        assert(k!=null);
        
        kl = bf.listKeypairs();
        //new keypair must exist in the list
        assert (kl.length == oldSize + 1);
        boolean found = false;
        for (KeyPairInfo k1: kl){
        	if (k1.getKeyName().equals("Cloud42Testpair")){
        		found = true;
        	}
        }
        
        assert (found);
        
        //describe keypair
        KeyPairInfo ki = bf.describeKeypair("Cloud42Testpair");
        assert (ki.getKeyName().equals("Cloud42Testpair"));
        
       
        
        //delete it again
        bf.deleteKeypair("Cloud42Testpair");
        
        kl = bf.listKeypairs();
        //list has to have old size
        assert (Arrays.asList(kl).size() == oldSize);
        
        found = false;
        for (KeyPairInfo k1: kl){
        	if (k1.getKeyName().equals("Cloud42Testpair")){
        		found = true;
        	}
        }
        
        assert (!found);
        
    }
    
    
//    /**
//     * Test registering and deregistering an AMI.
//     *
//     * NOTE: this test was commented out in version 1.2.0 because it has too many side effect.
//     * Plus, it will fail if one of the AMI images has an error
//     * (e.g. it is registered but the manifest file does not exist)
//     */
//    public void testRegisterAMI()
//    {
//    	
//        //at first get an AMI and its location
//        ImageDescription[] il = bf.listImages(false);
//        
//        //no image found -> no test
//       
//        int oldSize = Arrays.asList(il).size();
//        
//        if (oldSize ==0) return;
//        
//        
//        String location = il[0].getImageLocation();
//        
//        
//        //now register a new AMI for this location
//        String imageId = bf.registerImage(location);
//        
//        assert (imageId!= null);
//        
//        //new AMI must be in the list
//        il = bf.listImages(false);
//        
//        assert (Arrays.asList(il).size() == oldSize + 1);
//        boolean found = false;
//        for (ImageDescription d:il){
//        	if (d.getImageId().equals(imageId)){
//        		found = true;
//        		break;
//        	}
//        }
//        assert(found);
//        
//        
//        //deregister AMI again
//        bf.deregisterImage(imageId);
//        
//        il = bf.listImages(false);
//        assert (Arrays.asList(il).size() == oldSize);
//        
//        found = false;
//        for (ImageDescription d:il){
//        	if (d.getImageId().equals(imageId)){
//        		found = true;
//        		break;
//        	}
//        }
//        assert(!found);
//        
//    }
    
    
    /**
     * Test creating, listing and deleting security groups.
     * Applying groups to instances is done in <code>testControlInstances()</code>.
     */
    public void testSecurityGroups()
    {
    	
    	 
        GroupDescription[] gl = bf.listSecurityGroups();
        
        //save number of existing groups before test
        int oldSize = gl.length;
        
        //create group
        bf.createSecurityGroup("coolGroup", "coolDescription");
        
        
        gl = bf.listSecurityGroups();
        
        //new group must exist in the list
        assert (gl.length == oldSize + 1);
        boolean found = false;
        for (GroupDescription d: gl){
        	if (d.getName().equals("coolGroup") && d.getDescription().equals("coolDescription") ){
        		found = true;
        	}		
        }
        assert(found);
        
        //add permissions
        bf.addPermission("coolGroup", "tcp",8080,8080,"0.0.0.0/0");
        
        
        //describe group
        GroupDescription g = bf.describeSecurityGroup("coolGroup");
        assert (g.getName().equals("coolGroup") && g.getDescription().equals("coolDescription"));
        
        //check if perssion was applied
        assert (g.getPermissions().get(0).getToPort()==8080);
        
        
        //remove permission
        bf.removePermission("coolGroup", "tcp",8080,8080,"0.0.0.0/0");
        
        //describe group
        g = bf.describeSecurityGroup("coolGroup");
        
        //check if permission was removed
        assert (g.getPermissions().size()==0);
        
        
        
        //test source "user and group" mode
        bf.addPermission("coolGroup", "default", bf.getCredentials().getUserID());
        
        //describe group
        g = bf.describeSecurityGroup("coolGroup");
        assert (g.getName().equals("coolGroup") && g.getDescription().equals("coolDescription"));
        
        //check if perssion was applied
        assert (g.getPermissions().get(0).getUidGroupPairs().get(0)[1].equals("default"));
        assert (g.getPermissions().get(0).getUidGroupPairs().get(0)[0].equals(bf.getCredentials().getUserID()));
        
        
        
        
        //delete group again
        bf.deleteSecurityGroup("coolGroup");
        
        gl = bf.listSecurityGroups();
        
        //list has to have old size
        assert (gl.length == oldSize);
        found = false;
        for (GroupDescription d: gl){
        	if (d.getName().equals("coolGroup") && d.getDescription().equals("coolDescription") ){
        		found = true;
        	}		
        }
        assert(!found);
    	
    }
    
}
