/**
 * 
 */
package de.jw.cloud42.core.domain;

/**
 * A RemoteResult encapsulates return information of remotly executed commands.
 *  
 * @author fbitzer
 *
 */
public class RemoteResult {
	
	private String stdErr;
	private String stdOut;
	private int exitCode = -1;//set to -1 per default
	
	private String exceptionMessage;
	
	/**
	 * @return the stdErr
	 */
	public String getStdErr() {
		return stdErr;
	}
	/**
	 * @param stdErr the stdErr to set
	 */
	public void setStdErr(String stdErr) {
		this.stdErr = stdErr;
	}
	/**
	 * @return the stdOut
	 */
	public String getStdOut() {
		return stdOut;
	}
	/**
	 * @param stdOut the stdOut to set
	 */
	public void setStdOut(String stdOut) {
		this.stdOut = stdOut;
	}
	/**
	 * @return the exceptionMessage
	 */
	public String getExceptionMessage() {
		return exceptionMessage;
	}
	/**
	 * @param exceptionMessage the exceptionMessage to set
	 */
	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}
	public int getExitCode() {
		return exitCode;
	}
	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}
	
	
	
	
	

}
