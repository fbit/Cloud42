/**
 * 
 */
package de.jw.cloud42.core.domain;

import java.util.Calendar;



/**
 * 
 * Combines information contained in a ReservatioDescription and in a ReservatioDescription.Instance
 * within one class.
 * 
 * Wrapper around ReservationDescription.Instance of the Typica library since Axis2 does not recognize
 * nested types.
 * Only for use within the web service and the web application layer.
 * 
 * @author fbitzer
 *
 */
public class Instance {
	
	

	private String imageId;
    private String instanceId;
    private String privateDnsName;
    private String dnsName;
    private String reason;
    private String keyName;
    //private InstanceType instanceType;
    
    /**
     * use String representation because of problems with Axis
     */
    private String instanceType;
    private Calendar launchTime;
    private String availabilityZone;
    private String kernelId;
    private String ramdiskId;
    /**
     * An EC2 instance may be in one of four states:
     * <ol>
     * <li><b>pending</b> - the instance is in the process of being
     * launched.</li>
     * <li><b>running</b> - the has been launched. It may be in the
     * process of booting and is not yet guaranteed to be reachable.</li>
     * <li><b>shutting-down</b> - the instance is in the process of
     * shutting down.</li>
     * <li><b>terminated</b> - the instance is no longer running.</li>
     * </ol>
     */
    private String state;
    private int stateCode;
    
    
    //properties from ReservationDescription
    //the groups of this instance
    private String[] groups;
    
    private String reservationId;
    private String owner;

    public String getReservationId() {
		return reservationId;
	}

	public String getOwner() {
		return owner;
	}

	public String[] getGroups() {
		return groups;
	}

	/**
     * Set member variables in constructur.
     * @param resDescr The "original" ReservationDescription of the instance that is wrapped.
     * @param instanceIndex index of the instance within the ReservationDescription (usually 0,
     * but may be >0 if more than one instances are contained within one ReservationDescription)
     */
    public Instance(com.xerox.amazonws.ec2.ReservationDescription resDescr, int instanceIndex) {
        com.xerox.amazonws.ec2.ReservationDescription.Instance instance =
        	resDescr.getInstances().get(instanceIndex);    
    	
        this.groups = resDescr.getGroups().toArray(new String[0]);
        
        this.owner = resDescr.getOwner();
        this.reservationId=resDescr.getReservationId();
        
        this.imageId = instance.getImageId();
        this.instanceId = instance.getInstanceId();
        this.privateDnsName = instance.getPrivateDnsName();
        this.dnsName = instance.getDnsName();
        this.state = instance.getState();
        this.stateCode = instance.getStateCode();
        this.reason = instance.getReason();
        this.keyName = instance.getKeyName();
        this.instanceType = instance.getInstanceType().getTypeId();
        this.launchTime = instance.getLaunchTime();
        this.availabilityZone = instance.getAvailabilityZone();
        this.kernelId = instance.getKernelId();
        this.ramdiskId = instance.getRamdiskId();
            
            
    }

    public String getImageId() {
            return imageId;
    }

    public String getInstanceId() {
            return instanceId;
    }

    public String getPrivateDnsName() {
            return privateDnsName;
    }

    public String getDnsName() {
            return dnsName;
    }

    public String getReason() {
            return reason;
    }

    public String getKeyName() {
            return keyName;
    }

    public String getState() {
            return state;
    }

    public boolean isRunning() {
            return this.state.equals("running");
    }

    public boolean isPending() {
            return this.state.equals("pending");
    }

    public boolean isShuttingDown() {
            return this.state.equals("shutting-down");
    }

    public boolean isTerminated() {
            return this.state.equals("terminated");
    }

    public int getStateCode() {
            return stateCode;
    }

    public String getInstanceType() {
            return this.instanceType;
    }

    public Calendar getLaunchTime() {
            return this.launchTime;
    }

    public String getAvailabilityZone() {
            return availabilityZone;
    }

    public String getKernelId() {
            return kernelId;
    }

    public String getRamdiskId() {
            return ramdiskId;
    }

    public String toString() {
            return "[img=" + this.imageId + ", instance=" + this.instanceId
                            + ", privateDns=" + this.privateDnsName
                            + ", dns=" + this.dnsName + ", loc=" + ", state="
                            + this.state + "(" + this.stateCode + ") reason="
                            + this.reason + "]";
    }

//    public Instance getInstance(){
//    	return instance;
//    }
	
	
}
