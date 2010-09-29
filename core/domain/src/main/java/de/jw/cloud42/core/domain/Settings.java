package de.jw.cloud42.core.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.Query;
import org.hibernate.Session;


import de.jw.cloud42.core.hibernate.HibernateUtil;

/**
 * Holds global application settings.
 * Uses singleton pattern to keep instance state during application lifetime and to avoid fetching settings from
 * database on each call/instantiation.
 * 
 * @author fbitzer
 *
 */
@Entity
public class Settings {
	
	private static Settings theInstance;
	
	//Id field for hibernate, should always be 1
	private int id;
	
	/**
	 * The address of the Cloud42 message endpoint (used for Notification Mechanism in Web service layer)
	 */
	private String endpointAddress;

	
	
	/**
	 * Singleton method, loads settings from database if called for the first time.
	 * @return
	 */
	public static Settings getInstance(){
		if (theInstance == null){
			
			Session session = HibernateUtil.getSessionFactory().getCurrentSession();

	        session.beginTransaction();

	        Query q = session.createQuery("SELECT s FROM Settings s WHERE s.id = :sid");
		    q.setParameter("sid", 1);

		    List<Settings> resultset = q.list();
		    
		    
	        session.getTransaction().commit();
	        
	        if (resultset.size()>0) {
	        	theInstance = resultset.get(0);
	        } else {
	        	//no entry in database, so create new and empty settings, but set Id to 1
			    theInstance = new Settings();
	        	theInstance.setId(1);
		    }
		    
		  
		}
		
		return theInstance;
	}
	
	/**
	 * Saves current settings in database.
	 */
	public void save(){

    	Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        session.beginTransaction();

        session.saveOrUpdate(theInstance);
    	
        session.getTransaction().commit();
	}
	
	
	//getters and setters for settings properties.
	//**********************************************
	public String getEndpointAddress() {
		return endpointAddress;
	}

	public void setEndpointAddress(String endpointAddress) {
		this.endpointAddress = endpointAddress;
	}

	@Id
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
	
	
}
