/**
 * 
 */
package de.jw.cloud42.core.remoting;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.xerox.amazonws.ec2.ImageDescription;
import com.xerox.amazonws.ec2.InstanceType;
import com.xerox.amazonws.ec2.KeyPairInfo;

import de.jw.cloud42.core.domain.AwsCredentials;
import de.jw.cloud42.core.domain.Instance;
import de.jw.cloud42.core.domain.RemoteResult;
import de.jw.cloud42.core.service.Cloud42BaseFunctions;
import de.jw.cloud42.core.service.Cloud42Settings;

/**
 * Unit testclass for RemoteControl.
 * 
 * @author fbitzer
 * 
 */
public class RemoteControlTest extends TestCase {

	Cloud42BaseFunctions bf;
	boolean runAll = false;

	// variables used for each test case
	String pairName = "";
	String key = "";
	Instance i;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public RemoteControlTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(RemoteControlTest.class);
	}

	/**
	 * Setup, create a BaseFunctions object with the credentials.
	 */
	protected void setUp() {

		bf = new Cloud42BaseFunctions();

		try {

			Cloud42Settings config = Cloud42Settings.getInstance("access.properties");

			AwsCredentials cred = new AwsCredentials();

			cred.setAwsAccessKeyId(config.getString("AWSKey"));
			cred.setSecretAccessKey(config.getString("SecretAccessKey"));
			cred.setUserID(config.getString("Username"));

			bf.setCredentials(cred);
			
			//test in EU region
			bf.setRegionUrl("ec2.eu-west-1.amazonaws.com");
			
			runAll = config.getBoolean("runFullTests");

			// now start a test instance
			if (runAll) {

				pairName = UUID.randomUUID().toString();
				KeyPairInfo kp = bf.createKeypair(pairName);

				key = kp.getKeyMaterial();

				// use the first AMI available
				ImageDescription[] l = bf.listImages(false);

				// if there is no image for the current user, skip the whole
				// test
				if (l.length == 0) {
					Logger
							.getAnonymousLogger()
							.info(
									"Testcase was skipped because no "
											+ "AMI could be found for the current user account.");
					bf.deleteKeypair(pairName);

					return;
				}

				assert (l.length > 0);

				Logger.getAnonymousLogger().info(
						"Launching a test instance...this may take some time.");
				Instance[] il = bf.runInstanceBlocking(l[0].getImageId(),
						new String[] { "default" }, pairName, null,
						InstanceType.DEFAULT, 1, "", "", "");

				// now instance should be up
				assert (il[0].isRunning());
				assert (bf.describeInstance(il[0].getInstanceId()).isRunning());

				i = il[0];

			}

		} catch (Exception ex) {

			Logger.getAnonymousLogger().severe(
					"Initializing test configuration failed.");

			assert (false);
		}
	}

	/**
	 * After test: shutdown test instance and delete temporary keypair.
	 * 
	 */
	protected void tearDown() {

		if (i != null) {
			bf.stopInstance(i.getInstanceId());
		}
		if (!pairName.equals("")) {
			bf.deleteKeypair(pairName);
		}
		
	}

	/**
	 * Test uploading and downloading files as well as remoting capabilities.
	 * 
	 * (Remoting and File Transfer are just executed in the same test case since
	 * they require identical prerequisites concerning running instances and so
	 * on).
	 */
	public void testRemoting() {

		if (!runAll) {
			Logger.getAnonymousLogger().info(
							"Testcase 'testRemoting' was skipped because "
									+ "it was told to do so in the configuration file.");
			return;
		}

		// so we can transfer a file
		byte[] data = getFileFromClasspath("cloud.png");

		if (data == null) {

			assert (false);
			return;
		}

		Logger.getAnonymousLogger().info("Executing remoting test...");

		RemoteControl c = new RemoteControl();
		String host = i.getDnsName();


		// use a random subdirectory
		String subdir = UUID.randomUUID().toString();

		// remove the file first if it should exist (test Remoting capabilities
		// this way)
		RemoteResult r = c.executeCommand(host, key, "rm /tmp/" + subdir
				+ "/cloud.png");

		assert (r.getExceptionMessage() == null);
		// file must be deleted
		r = c.executeCommand(host, key, "ls /tmp/" + subdir);

		assert (r.getExceptionMessage() == null);
		assert (!r.getStdOut().contains("cloud.png"));

		// upload the file
		r = c.uploadFile(i.getDnsName(), key, "/tmp/" + subdir, "cloud.png",
				data);
		assert (r.getExceptionMessage() == null);
		assert (r.getExitCode() == 0);

		// now it should be there

		r = c.executeCommand(host, key, "ls /tmp/" + subdir);

		assert (r.getExceptionMessage() == null);
		assert (r.getExitCode() == 0);
		assert (r.getStdOut().contains("cloud.png"));

		// now check if subdir folder exists using a batch
		byte[] batch = getFileFromClasspath("batch.sh");

		if (batch == null) {

			assert (false);
			return;
		}

		r = c.executeBatch(host, key, batch);
		
		assert (r.getExitCode() == 0);
		assert (r.getExceptionMessage() == null);
		assert (r.getStdOut().contains(subdir));

		// now download file again
		byte[] download = c.downloadFile(host, key, "/tmp/"+ subdir + "/cloud.png");

		assert (download != null);
		
		boolean areEqual = areEqual(download, data);
		
		assert (areEqual);

		// and delete it
		r = c.executeCommand(host, key, "rm /tmp/" + subdir + "/cloud.png");

		assert (r.getExitCode() == 0);
		assert (r.getExceptionMessage() == null);
		// file must be deleted
		r = c.executeCommand(host, key, "ls /tmp/" + subdir);

		assert (!r.getStdOut().contains("cloud.png"));
		
		//execute an unknown command to test exitCode
		r = c.executeCommand(host, key, "testabc");
		assert (r.getExitCode() != 0);
		
		//now try an invalid command
		r = c.executeCommand(host, key, "wget mydumpurl");
		assert (r.getExitCode() == 1);
		

	}

	/**
	 * Helper function, reads a file from classpath and returns its content as
	 * byte array.
	 * 
	 * @param filename
	 * @return
	 */
	private byte[] getFileFromClasspath(String filename){
    	
    	 try {
         	InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(filename);
             
         	
    		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
    		byte[] bytes = new byte[512];

    		// Read bytes from the input stream in bytes.length-sized chunks and
    		// write
    		// them into the output stream
    		int readBytes;
    		while ((readBytes = inputStream.read(bytes)) > 0) {
    			outputStream.write(bytes, 0, readBytes);
    		}

    		// Convert the contents of the output stream into a byte array
    		byte[] byteData = outputStream.toByteArray();

    		// Close the streams
    		inputStream.close();
    		outputStream.close();

    		return byteData;
         	
         } catch (Exception ex){
         	Logger.getAnonymousLogger().severe("Error reading file " + filename);
         	
         	
         	return null;
         }
    	
    }

	/**
	 * Helper function, checks whether two byte arrays are equal.
	 * @param a1
	 * @param a2
	 * @return
	 */
	private boolean areEqual(byte[] a1, byte[] a2) {
		
		int len = a1.length;
		if (a1.length != a2.length){
			return false;
		}
		
		for (int i=0; i < len; i++){
			
			if (a1[i] != a2[i]) return false;
			
		}
		
		return true;
	}
	
}
