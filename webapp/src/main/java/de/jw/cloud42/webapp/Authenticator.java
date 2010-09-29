package de.jw.cloud42.webapp;

import java.util.List;

import org.hibernate.Session;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.security.Identity;

import de.jw.cloud42.core.domain.AwsCredentials;
import de.jw.cloud42.core.domain.User;



/**
 * Seam Authenticator component.
 * 
 */
@Name("authenticator")
//@Scope(ScopeType.SESSION)
public class Authenticator {
	
	/**
	 * injected Hibernate session
	 */
	@In 
	Session session;
	
	/**
	 * userManager is initialized by login method and outjected again.
	 */
	@In(create=true)
	@Out
	UserManager userManager;
	
	/**
	 * Seam's authentication method.
	 * Login a user and set credentials by initializing userManager.
	 * @return
	 */
	public boolean authenticate() {
      
		List users = session.createQuery(
        	"from User where name = :name and password = :password")
        	.setParameter("name", Identity.instance().getUsername())
        	.setParameter("password", Identity.instance().getPassword())
        	.list();

		
		if (users.size() == 1){
			User user = (User) users.get(0);
			userManager.setUser(user);
			
			if (user.getCredentials()==null){
				//no AWS credentials saved for this user -> set new ones 
				userManager.setCurrentCredentials(new AwsCredentials());
			}else {
				//user has already AWS credentials stored in DB -> set current credentials.
				userManager.setCurrentCredentials(user.getCredentials());
			}
			return true;
		} else {//no unique user found
			//userManager.setUser(null);
			//userManager.setCurrentCredentials(null);
			return false;
		}
		
		
   }
   
}