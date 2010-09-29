/**
 * 
 */
package de.jw.cloud42.webservice;


import de.jw.cloud42.core.domain.AwsCredentials;
import de.jw.cloud42.core.domain.RemoteResult;
import de.jw.cloud42.core.remoting.RemoteControl;

/**
 * Service class for Remoting Service, provides methods to execute arbitrary commands on an AMI instance.
 * 
 * @author fbitzer
 *
 */
public class Cloud42RemotingService {
	
	/**
	 * Executes the given command on the given remote host.
	 * @param dnsName Hostname or IP address of the target AMI instance.
	 * @param rsaKey RSA private key as String.
	 * @param command the command to execute.
	 * @return RemoteResult object containing the output of the command.
	 */
	public RemoteResult executeCommand(String dnsName, String rsaKey, String command){
		
		RemoteControl c = new RemoteControl();
		
		return c.executeCommand(dnsName, rsaKey, command);
		
	}
	
	
	/**
	 * Executes a batch file / shell script on the instance.
	 * @param dnsName Hostname or IP address of the target AMI instance.
	 * @param rsaKey RSA private key as String.
	 * @param batchFile Shellscript to execute. Transferred as SOAP attachment using MTOM.
	 * @return RemoteResult object containing the output of the script.
	 */
	public RemoteResult executeBatch(String dnsName, String rsaKey, byte[] batchFile){
		
		RemoteControl c = new RemoteControl();
		
		return c.executeBatch(dnsName, rsaKey, batchFile);
			
	}
	
	
	/**
	 * Bundles a new image from a running AMI.
	 * 
	 * @param dnsName Hostname of AMI to bundle.
	 * @param rsaKey RSA private key to connect to AMI instance.
	 * @param credentials AWS credentials.
	 * @param targetBucket Bucket on S3 where new AMI should be stored.
	 * @param newImageName Name of new image.
	 * @param is64Bit Set to true, if a 64 bit image should be created.
	 * @param notifyWhenFinished Trigger a notification message when bundling has finished.
	 * @param topic Topic for the notification.
	 * @param messageText Notification text.
	 * @param messageInfo Additional notification information.
	 * @param keyFile The user's private key file for authorization at the EC2 API.
	 * @param certFile The user's certificate file for authorization at the EC2 API.
	 * @return RemoteResult object indicating success or an exception.
	 */
	public RemoteResult bundleImage(String dnsName, String rsaKey, AwsCredentials credentials, 
			String targetBucket, String newImageName, boolean is64Bit,
			boolean notifyWhenFinished, String topic, String messageText, String messageInfo,
			byte[] keyFile, byte[] certFile){
		
		RemoteControl c = new RemoteControl();
		
		return c.bundleImage(dnsName, rsaKey, credentials, targetBucket, newImageName, is64Bit,
				notifyWhenFinished,  topic, messageText,  messageInfo,
				keyFile, certFile);
	}

}
