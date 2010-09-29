/**
 * 
 */
package de.jw.cloud42.webservice;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.attachments.ConfigurableDataHandler;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.wsdl.WSDLConstants;

import de.jw.cloud42.core.domain.RemoteResult;
import de.jw.cloud42.core.remoting.RemoteControl;

/**
 * Service class for Cloud42 Web service file transfer functions.
 * 
 * @author fbitzer
 *
 */
public class Cloud42FileService {

	/**
	 * Upload a file to an AMI instance.
	 * @param dnsName Hostname or IP address of remote host.
	 * @param rsaKey RSA private key to use for authentification.
	 * @param targetDir Target folder on remote machine.
	 * @param targetFilename Target filename on remote machine.
	 * @param fileData The uploaded file.
	 * @return {@link RemoteResult} with empty output if execution was successful or an 
	 * exception message if not.
	 */
	public RemoteResult uploadFile(String dnsName, String rsaKey, String targetDir, String targetFilename, byte[] fileData){
		
		RemoteControl c = new RemoteControl();
		
		return c.uploadFile(dnsName, rsaKey, targetDir, targetFilename, fileData);	
		
	}
	
	/**
	 * Upload a file from an URL to an AMI instance.
	 * Can be used to transfer files from S3 buckets to an instance.
	 * Uses wget for file transfer.
	 * 
	 * @param dnsName Hostname or IP address of remote host/the running instance.
	 * @param rsaKey RSA private key to use for authentification.
	 * @param targetDir Target folder on remote machine.
	 * @param targetFilename Target filename on remote machine.
	 * @param url URL for download.
	 * @return {@link RemoteResult} encapsulating the output of the wget command.
	 */
	public RemoteResult uploadFileFromURL(String dnsName, String rsaKey, String targetDir, String targetFilename, String url){
		
		RemoteControl c = new RemoteControl();
		
		return c.uploadFileFromURL(dnsName, rsaKey, targetDir, targetFilename, url);	
		
	}
	
//	/**
//	 * Download a file from a remote host.
//	 * 
//	 * @param element SOAP message containing the following tags (in the namespace http://webservice.cloud42.jw.de):
//	 * <ul>
//	 * <li>dnsName: hostname or IP address of remote host.</li>
//	 * <li>rsaKey: RSA key to use.</li>
//	 * <li>file: absolute path of file to retreive.</li>
//	 * </ul>
//	 * @return MTOM-optimized SOAP message containing the requestet file.
//	 */
//	public OMElement downloadFile(OMElement element){
//		
//		
//		//parse incoming OMElement
//		QName qn = new QName("http://webservice.cloud42.jw.de","rsaKey");
//		String key =  element.getFirstChildWithName(qn).getText();
//		
//		qn = new QName("http://webservice.cloud42.jw.de","dnsName");
//		String host =  element.getFirstChildWithName(qn).getText();
//		
//		qn = new QName("http://webservice.cloud42.jw.de","file");
//		String file =  element.getFirstChildWithName(qn).getText();
//		
//		
//		
//		//invoke business logic to retreive file.
//		RemoteControl c = new RemoteControl();
//		
//		byte[] data =  c.downloadFile(host, key, file);
//		
//		
//		try {
//			//create a SOAP message for the response
//			OMFactory factory = element.getOMFactory();
//	        OMNamespace ns = factory.createOMNamespace("http://webservice.cloud42.jw.de", "web");
//	        OMElement payload  = factory.createOMElement("uploadFileResponse", ns);
//	        OMElement response = factory.createOMElement("return", ns);
//	        OMElement fileElement = factory.createOMElement("file", ns);
//
//	        
//	        DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(data));
//	        OMText textData = factory.createOMText(dataHandler, true);
//	        fileElement.addChild(textData);
//	        response.addChild(fileElement);
//	        payload.addChild(response);
//
//
//			//enable MTOM -> Axis will automatically optimize the message
//			MessageContext outMsgCtx = MessageContext.getCurrentMessageContext()
//            	.getOperationContext().getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
//			
//			outMsgCtx.setProperty(
//					org.apache.axis2.Constants.Configuration.ENABLE_MTOM,
//					org.apache.axis2.Constants.VALUE_TRUE);
//
//        	return payload;
//
//			
//		} catch (Exception ex){
//			
//			ex.printStackTrace();
//			
//			return null;
//		}
//
//	}
	
	
	/**
	 * Download a file from a remote host.
	 * 
	 * @param dnsName Hostname or IP address of remote host.
	 * @param rsaKey RSA private key to use for authentification.
	 * @param fileName full path and name of file to download.
	 * 
	 * @return MTOM message with requested file as attachment.
	 */
	public DataHandler downloadFile(String dnsName, String rsaKey, String fileName) {
		
		RemoteControl c = new RemoteControl();
		
		byte[] data =  c.downloadFile(dnsName, rsaKey, fileName);
		
		ByteArrayDataSource dataSource = new ByteArrayDataSource(data);
		DataHandler dataHandler = new DataHandler(dataSource);
		 
		return dataHandler;
	
	}

}
