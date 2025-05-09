package org.example.chat_application.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.example.chat_application.model.User;
import org.example.chat_application.model.Chat;
import org.example.chat_application.model.Message;
import org.example.chat_application.model.Subscription;

public class HibernateUtil {
    private static SessionFactory sessionFactory = null;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                System.out.println("Creating Hibernate configuration...");
                // Create configuration instance
                Configuration configuration = new Configuration();

                // Try to load configuration from a file
                try {
                    // Load from the classpath root
                    System.out.println("Attempting to load hibernate.cfg.xml...");
                    configuration.configure();
                    System.out.println("Successfully loaded hibernate.cfg.xml");
                } catch (Exception e) {
                    System.err.println("Failed to load hibernate.cfg.xml: " + e.getMessage());
                    e.printStackTrace();
                    System.out.println("Will use programmatic configuration instead");
                }

                // Set up database connection programmatically to ensure it works even if the XML file isn't found
                configuration.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
                configuration.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/chat_app?useSSL=false&serverTimezone=UTC");
                configuration.setProperty("hibernate.connection.username", "root");
                configuration.setProperty("hibernate.connection.password", "root0000");
                configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
                configuration.setProperty("hibernate.show_sql", "true");
                configuration.setProperty("hibernate.hbm2ddl.auto", "update");
                configuration.setProperty("hibernate.connection.provider_class", "org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl");
                configuration.setProperty("hibernate.current_session_context_class", "thread");
                configuration.setProperty("hibernate.connection.pool_size", "10");

                // Add annotated classes
                configuration.addAnnotatedClass(User.class);
                configuration.addAnnotatedClass(Chat.class);
                configuration.addAnnotatedClass(Message.class);
                configuration.addAnnotatedClass(Subscription.class);

                // Build service registry
                System.out.println("Building service registry...");
                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties())
                        .build();
                System.out.println("Service registry built successfully");

                // Create session factory
                System.out.println("Building session factory...");
                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
                System.out.println("Session factory built successfully");

                return sessionFactory;
            } catch (Exception ex) {
                System.err.println("Initial SessionFactory creation failed: " + ex.getMessage());
                System.err.println("Exception type: " + ex.getClass().getName());
                System.err.println("Stack trace:");
                ex.printStackTrace();

                // Check for nested exceptions
                Throwable cause = ex.getCause();
                if (cause != null) {
                    System.err.println("Caused by: " + cause.getMessage());
                    cause.printStackTrace();
                }

                throw new ExceptionInInitializerError(ex);
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
