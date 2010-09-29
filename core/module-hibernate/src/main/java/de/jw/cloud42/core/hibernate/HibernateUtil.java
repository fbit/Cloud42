package de.jw.cloud42.core.hibernate;


import org.hibernate.*;
import org.hibernate.cfg.*;

/**
 * 
 * Hibernate Util provides access to Hibernate's SessionFactory.
 * 
 * This utility class is only used from the webservice application, 
 * since the webapp uses Seam to integrate Hibernate.
 * 
 * @author fbitzer
 *
 */
public class HibernateUtil {

    private static final SessionFactory sessionFactory;

    static {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            sessionFactory = new  AnnotationConfiguration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

}
