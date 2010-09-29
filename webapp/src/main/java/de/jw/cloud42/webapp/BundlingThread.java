/**
 * 
 */
package de.jw.cloud42.webapp;

import java.util.logging.Logger;

import de.jw.cloud42.core.domain.AwsCredentials;
import de.jw.cloud42.core.domain.RemoteResult;
import de.jw.cloud42.core.remoting.RemoteControl;

/**
 * Thread class, handles bundling a new AMI as background task.
 * 
 * @author fbitzer
 * 
 */
public class BundlingThread extends Thread {

	// member variables containing the required information for bundling an AMI
	private String dnsName;
	private String privateKey;
	private AwsCredentials credentials;
	private String bucket;
	private String imageName;
	private boolean use64Bit;
	private boolean doNotify;
	private String messageTopic;
	private String messageText;
	private String messageInfo;
	private byte[] privateKeyFile;
	private byte[] certFile;

	/**
	 * Holds the result of the bundling process
	 */
	private RemoteResult result;

	/**
	 * Construktor, gathers required information at startup.
	 * @param dnsName
	 * @param privateKey
	 * @param credentials
	 * @param bucket
	 * @param imageName
	 * @param use64Bit
	 * @param doNotify
	 * @param messageTopic
	 * @param messageText
	 * @param messageInfo
	 * @param privateKeyFile
	 * @param certFile
	 */
	public BundlingThread(String dnsName, String privateKey,
			AwsCredentials credentials, String bucket, String imageName,
			boolean use64Bit, boolean doNotify, String messageTopic,
			String messageText, String messageInfo, byte[] privateKeyFile,
			byte[] certFile) {

		this.dnsName = dnsName;
		this.privateKey = privateKey;
		this.credentials = credentials;

		this.bucket = bucket;
		this.imageName = imageName;

		this.use64Bit = use64Bit;
		this.doNotify = doNotify;

		this.messageTopic = messageTopic;
		this.messageText = messageText;
		this.messageInfo = messageInfo;

		this.privateKeyFile = privateKeyFile;

		this.certFile = certFile;

	}

	/**
	 * This is the thread function that is invoked with Thread.start().
	 * It starts bundling by invoking the <code>RemoteControl</code> class.
	 */
	public void run() {

		Logger.getAnonymousLogger().info(
				"Bundling thread started for AMI at " + dnsName);

		RemoteControl c = new RemoteControl();

		result = c.bundleImage(dnsName, privateKey, credentials, bucket,
				imageName, use64Bit, doNotify, messageTopic, messageText,
				messageInfo, privateKeyFile, certFile);

		Logger.getAnonymousLogger().info(
				"Bundling thread completed for AMI at " + dnsName);

	}

	/**
	 * Getter for the result of the bundling process.
	 * @return
	 */
	public RemoteResult getResult() {
		return result;
	}

}
