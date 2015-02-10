package org.jazzteam.utils;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.internal.StandardServiceRegistryImpl;
import org.hibernate.cfg.Configuration;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtil {
    private static final SessionFactory sessionFactory;
    private static final String CONFIGURATION_FILE_NAME = "/hibernate.cfg.xml";
    private static final ServiceRegistry serviceRegistry;

    static {
        try {
            Configuration cfg = new Configuration().configure(CONFIGURATION_FILE_NAME);
            serviceRegistry =
                    new StandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build();
            sessionFactory = cfg.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static FullTextSession session = null;

    public static FullTextSession getSession() throws InterruptedException {
        if (session == null) {
            session = Search.getFullTextSession(sessionFactory.openSession());
            session.setFlushMode(FlushMode.ALWAYS);
            session.createIndexer().startAndWait();
        }
        return session;
    }

    public static void closeSession() {
        if (session != null) {
            session.close();
        }
        if (serviceRegistry != null) {
            StandardServiceRegistryBuilder.destroy(serviceRegistry);
        }
    }

}
