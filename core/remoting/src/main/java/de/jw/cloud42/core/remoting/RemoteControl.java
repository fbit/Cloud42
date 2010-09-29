/**
 * 
 */
package de.jw.cloud42.core.remoting;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.hibernate.type.EmbeddedComponentType;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

import de.jw.cloud42.core.domain.AwsCredentials;
import de.jw.cloud42.core.domain.Instance;
import de.jw.cloud42.core.domain.RemoteResult;
import de.jw.cloud42.core.domain.Settings;
import de.jw.cloud42.core.service.Cloud42BaseFunctions;

/**
 * 
 * Class providing methods for remote control of AMI instances by using SSH.
 * Also contains methods for up- and downloading files .
 * 
 * @author fbitzer
 * 
 */
public class RemoteControl {

	/**
	 * Folder for files temporary stored on a AMI instance.
	 */
	private final String TEMP_REMOTE_FOLDER = "/tmp";
	
	/**
	 * Filename for temporary batch files.
	 */
	private final String TEMP_BATCH_FILENAME = "tmp.sh";
	
	
	/**
	 * Executes the given command on the given remote host.
	 * @param host Hostname or IP address of the target AMI instance.
	 * @param key RSA private key as String.
	 * @param command the command to execute.
	 *
	 * @return RemoteResult object containing the output of the command.
	 */
	public RemoteResult executeCommand(String host, String key, String command) {

		Connection conn = connect(host,key);
		
		RemoteResult result =  exec(conn,command);
		
		/* Close the connection */

		if (conn != null) conn.close();

		return result;
		
	}
		
	/**
	 * Executes a batch file / shell script on the instance.
	 * @param host Hostname or IP address of the target AMI instance.
	 * @param key RSA private key as String.
	 * @param batchData Shellscript to execute.
	 * @return RemoteResult object containing the output of the script.
	 */
	public RemoteResult executeBatch(String host, String key, byte[] batchData) {
		
		Connection conn = connect(host, key);
		
		RemoteResult result = new RemoteResult();
		
		try {
			
			if (conn == null) {
				throw new IOException("Connection failed.");
			}
		
			//Upload batch file			
			RemoteResult r1 = doUpload(conn, batchData, TEMP_BATCH_FILENAME, TEMP_REMOTE_FOLDER);
			
			if (r1.getExceptionMessage() != null) {
				throw new Exception("Uploading batch file failed due to an exception: " + r1.getExceptionMessage());
			}
			
			//execute the uploaded batch file
			
			//set rights before executing
			r1 = exec(conn, "chmod 774 " + TEMP_REMOTE_FOLDER + "/" + TEMP_BATCH_FILENAME);
			

			if (!r1.getStdErr().equals("")) {
				throw new Exception("Setting rights for batch file failed: " + r1.getStdErr());
			}
			if (r1.getExceptionMessage() != null) {
				throw new Exception("Setting rights for batch file failed due to an exception: " + r1.getExceptionMessage());
			}
			
			result =  exec(conn, TEMP_REMOTE_FOLDER + "/" + TEMP_BATCH_FILENAME);
			
			
		} catch (Exception ex){
			
			result.setExceptionMessage(ex.getMessage());
			
		}
		
		//now delete temp file
		
		exec(conn, "rm -f " + TEMP_REMOTE_FOLDER + "/" + TEMP_BATCH_FILENAME);
		
		/* Close the connection */

		if (conn != null) conn.close();

		return result;
		
			
	}
	
	
	/**
	 * Uploads a file to an AMI instance.
	 * @param host Hostname or IP address of remote host.
	 * @param key RSA private key to use for authentification.
	 * @param targetDir Target folder on remote machine.
	 * @param targetFilename Target filename on remote machine.
	 * @param fileData The uploaded file.
	 * @return a RemoteResult encaplsulating error messages.
	 */
	public RemoteResult uploadFile(String host, String key, String targetDir, String targetFilename, byte[] fileData){
		
		Connection conn = connect(host, key);
		
		RemoteResult result = new RemoteResult();
		
		try {
			
			if (conn == null) {
				throw new IOException("Connection failed.");
			}
			
			//create target dir at first
			exec(conn, "mkdir -p " + targetDir);
			
			result = doUpload(conn, fileData, targetFilename, targetDir);
			
		} catch (Exception ex){
			
			ex.printStackTrace();
			
			result.setExceptionMessage(ex.getMessage());
			
		}
		
		if (conn != null) conn.close();

		return result;
	}
	
	
	/**
	 * Uploads a file to an AMI instance from a given URL.
	 * Can be used to transfer files from S3 to an EC2 instance.
	 * Uses wget for file transfer.
	 * 
	 * @param host Hostname or IP address of remote host.
	 * @param key RSA private key to use for authentification.
	 * @param targetDir Target folder on remote machine.
	 * @param targetFilename Target filename on remote machine.
	 * @param url URL of file to transfer.
	 * @return a RemoteResult encaplsulating the output of the wget command.
	 */
	public RemoteResult uploadFileFromURL(String host, String key, String targetDir, String targetFilename, String url){
		
		Connection conn = connect(host, key);
		
		RemoteResult result = new RemoteResult();
		
		try {
			
			if (conn == null) {
				throw new IOException("Connection failed.");
			}
		
			
				
			exec(conn, "mkdir -p " + targetDir);
			
			result = exec(conn, "wget " + url + " -O " + targetDir + "/" + targetFilename);
			
		} catch (Exception ex){
			
			ex.printStackTrace();
			
			
			result.setExceptionMessage(ex.getMessage());
			
		}
		
		if (conn != null) conn.close();

		return result;
	}
	
	/**
	 * Download a file from a remote host.
	 * 
	 * @param host Hostname.
	 * @param key RSA key to use.
	 * @param remoteFileName Absolute filename of file to download.
	 * @return File as byte array.
	 */
	public byte[] downloadFile(String host, String key, String remoteFileName){
		
		Connection conn = connect(host, key);
		
		try {
			
			if (conn == null) {
				throw new IOException("Connection failed.");
			}
		
			/* Create a SCPClient */
			SCPClient scp = conn.createSCPClient();
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			scp.get(remoteFileName, out);
			
			if (conn != null) conn.close();

			
			return out.toByteArray();
			
		
		} catch (Exception ex){
			
			ex.printStackTrace();
			
			if (conn != null) conn.close();

			
			return null;
			
		}
		
	}
	
	/**
	 * Bundle a new AMI from an existing one by executing the neccessary statements.
	 * 
	 * @param dnsName
	 * @param key
	 * @param credentials
	 * @param targetBucket
	 * @param newImageName
	 * @param use64Bit
	 * @param notifyWhenFinished
	 * @param messageText
	 * @param messageInfo
	 * @param keyFile
	 * @param certFile
	 * @return RemoteResult with ExceptionMessage in case of error. Output of the single statements is NOT transferred.
	 */
	public RemoteResult bundleImage(String dnsName, String key, AwsCredentials credentials, 
								String targetBucket, String newImageName, boolean use64Bit, 
								boolean notifyWhenFinished, String topic, String messageText, String messageInfo,
								byte[] keyFile, byte[] certFile){
		
		Connection conn = connect(dnsName, key);
		
		RemoteResult result = new RemoteResult();
		result.setStdErr("");
		result.setStdOut("");
		result.setExitCode(0);
		
		try {
			
			if (conn == null) {
				throw new IOException("Connection failed.");
			}
		
			//Copy pk file and certificate
			
			/* Create a SCPClient */
			SCPClient scp = conn.createSCPClient();
			
			//private key
			scp.put(keyFile, "pk.pem", "//mnt");
			
			//certificate
			scp.put(certFile, "cert.pem", "//mnt");
			
			String architecture;
			if (use64Bit) {
				architecture = "x86_64";
			} else {
				architecture = "i386";
			}
			
			RemoteResult tmpResult;
			
			//bundle AMI
			tmpResult = exec(conn,"ec2-bundle-vol -d //mnt -k //mnt//pk.pem -c //mnt//cert.pem -u " 
					+ credentials.getUserID() + " -r " + architecture + " -p " + newImageName);
			
			
			if (tmpResult.getExceptionMessage() != null) {
				throw new Exception("ec2-bundle-vol failed with exception: " + tmpResult.getExceptionMessage());
			}
			
			if (tmpResult.getExitCode() != 0) {
				throw new Exception("ec2-bundle-vol failed. Error output is: " + tmpResult.getStdErr());
			}
			
			//upload to S3
			tmpResult = exec(conn,"ec2-upload-bundle -b " + targetBucket + " -m //mnt//" 
					+ newImageName + ".manifest.xml -a " + credentials.getAwsAccessKeyId() 
					+ " -s " + credentials.getSecretAccessKey());
			
			
			if (tmpResult.getExceptionMessage() != null) {
				throw new Exception("ec2-upload-bundle failed with an exception: " +tmpResult.getExceptionMessage());
			}
			
			if (tmpResult.getExitCode() != 0) {
				throw new Exception("ec2-upload-bundle failed. Error output is: " + tmpResult.getStdErr());
			}
			
			
			//send notification if required
			if (notifyWhenFinished){
				
				//get instance-id at first
				String instanceId = "";
				//query instance metadata
				tmpResult = exec(conn, "curl http://169.254.169.254/latest/meta-data/instance-id");
				
				
				if (tmpResult.getExceptionMessage() != null) {
					throw new Exception("Retreiving instance-id failed: " + tmpResult.getExceptionMessage());
				}
				
				if (tmpResult.getStdOut()!= null) {
					instanceId = tmpResult.getStdOut();
				}
				
				//Format current date
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				java.util.Date date = new java.util.Date();
				String timestamp = dateFormat.format(date);

				//compose message
				String message = "<message xmlns=\\\"http://cloud42.jw.de/message\\\">" +
								  "<topic>" + topic + "</topic>" + 
								  "<instanceId>" + instanceId + "</instanceId>" + 
								  "<timestamp>" + timestamp + "</timestamp>" + 
								  "<text>" + messageText + "</text>" + 
								  "<info>" + messageInfo + "</info>" +
								  "</message>";
				
				
				//use the address of the Cloud42 notification endpoint to send the notifcation
				String endpointAddress = Settings.getInstance().getEndpointAddress();
				
				
				
				if (endpointAddress != null){
				
					String command = "curl -H \"Content-Type: text/xml\" -i -d \"" 
									+ message + "\" " + endpointAddress;
					
					
					tmpResult = exec(conn,command);
					
					if (tmpResult.getExitCode() != 0) {
						Logger.getAnonymousLogger().severe(
							"Bundling was completed but notification could not be sent.");	
					}
					
				} else {
					Logger.getAnonymousLogger().severe(
							"No endpoint address for Cloud42 notification endpoint was found. Notification could not be sent.");	
				}
				
				if (tmpResult.getExceptionMessage() != null) {
					throw new Exception("Notification failed with exception: " + tmpResult.getExceptionMessage());
				}
				
			}
			
			
			
		} catch (Exception ex){
			
			ex.printStackTrace();
			
			
			result.setExceptionMessage(ex.getMessage());
			result.setExitCode(1);
			
		}
		
		if (conn != null) conn.close();

		return result;
	}
	
	
	/**
	 * Private helper function, etablishes a connection to a remote host.
	 * @param host Hostname or IP address.
	 * @param key Private RSA key for auhentification.
	 * @return Connection object representing the connection or null in case of error.
	 */
	private Connection connect(String host, String key){
		
		try
		{
			
			/* Create a connection instance */

			Connection conn = new Connection(host);

			/* Now connect */

			conn.connect();

			/* Authenticate */

			boolean isAuthenticated = conn.authenticateWithPublicKey("root", key.toCharArray(), "");

			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");

			return conn;
			
		} catch (IOException e){
			
			e.printStackTrace(System.err);
				
			return null;
				
		}
	}
	
	/**
	 * 
	 * Executes the given command on a remote host using the given connection object.
	 * 
	 * @param conn the connection to use.
	 * @param command the command to execute.
	 * @return RemoteResult object containing the output of the command.
	 */
	private RemoteResult exec(Connection conn, String command){
		
		RemoteResult result = new RemoteResult();
		try {
			/* Create a session */
			if (conn == null) {
				throw new IOException("Connection failed.");
			}
		
				
			Session sess = conn.openSession();
	
			//This is the more complex approach, but it might be neccessary in some cases when a
			//special environment is needed.
			//See Trilead SSH FAQ included in distribution
//			Session session = conn.openSession();
//			
//			session.requestPTY("bash");
//			session.startShell();
//			
//			InputStream in = session.getStdout();
//			OutputStream out = session.getStdin();
//			InputStream error = session.getStderr();
//				
//			// Sending a command
//			
//			out.write((command + "\n").getBytes());
//
//			// getting the result of the command
//			BufferedReader br = new BufferedReader(new InputStreamReader(in));
//
//			String txt = "";
//			
//			while (true)
//			{
//				String line = br.readLine();
//				if (line == null)
//					break;
//				txt = txt + line + "\n";
//			}
//			result.setOutput(txt);
			//!!!!!!!!!!!!!!
			
			
			
			sess.execCommand(command);
	
			
			
			//read  stdout and stderr
			InputStream out;
			InputStream err;
			
			out = new StreamGobbler(sess.getStdout());
			err = new StreamGobbler(sess.getStderr());
			
			BufferedReader brOut = new BufferedReader(new InputStreamReader(out));
			BufferedReader brErr = new BufferedReader(new InputStreamReader(err));
	
	
			String outMessage = "";
			String errMessage = "";
	
			while (true)
			{
				String line = brOut.readLine();
				if (line == null)
					break;
				outMessage = outMessage + line + "\n";
			}
//			// remove last linebreak in order to return just the output without
//			// an additional line
//			if (!outMessage.equals("")){
//				outMessage = outMessage.substring(0, outMessage.length() - 1);
//			}
			while (true)
			{
				String line = brErr.readLine();
				if (line == null)
					break;
				errMessage = errMessage + line + "\n";
			}
			
			result.setStdErr(errMessage);
			result.setStdOut(outMessage);
			
			//set exit signal
			try {
				result.setExitCode(sess.getExitStatus());
			} catch (Exception ex){
				
			}
			
			
			//a boolean success tag would be nice, but it is not possible to
			//determine an execution's success by the outputs (some commands write to the stdErr in spite
			//of beeing executed successfully.
			
//			if (outMessage.equals("") && errMessage.equals("")){
//				
//				result.setSuccess(true);
//				//result.setOutput("");
//				
//			} else if (outMessage.equals("") && !errMessage.equals("")){
//				
//				result.setSuccess(false);
//				result.setOutput(errMessage);
//				
//			} else if (!outMessage.equals("") && errMessage.equals("")){
//				
//				result.setSuccess(true);
//				result.setOutput(outMessage);
//				
//			} else {
//				//both messages contain values -> assume everything went fine
//				result.setSuccess(true);
//				result.setOutput(outMessage);
//			}
	
			/* Close this session */
	
			sess.close();
	
		
		} catch (Exception ex){
			
			result.setExceptionMessage(ex.getMessage());
			
		}
		
		return result;
	}
		
	
	/**
	 * Uploads data by a SCP command.
	 * @param conn
	 * @param data
	 * @param fileName
	 * @param dir
	 * @return
	 */
	private RemoteResult doUpload(Connection conn, byte[] data, String fileName, String dir){
		
		RemoteResult result = new RemoteResult();
		
		try {
			
			/* Create a SCPClient */
			SCPClient scp = conn.createSCPClient();
			
			scp.put(data, fileName, dir);
		
			result.setExitCode(0);
			
		} catch (Exception ex){
			
			result.setExceptionMessage(ex.getMessage());
			
			result.setExitCode(1);
		}
		
		return result;
	}
	
}
