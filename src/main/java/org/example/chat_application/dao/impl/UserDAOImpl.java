package org.example.chat_application.dao.impl;

import org.example.chat_application.dao.UserDAO;
import org.example.chat_application.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class UserDAOImpl implements UserDAO {

    private final SessionFactory sessionFactory;

    public UserDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void saveUser(User user) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(user);
            tx.commit();
        }
    }

    @Override
    public User getUserById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(User.class, id);
        }
    }

    @Override
    public List<User> getAllUsers() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                "FROM User u ORDER BY u.username", 
                User.class)
                .setMaxResults(100)  // Limit results
                .getResultList();
        }
    }

    @Override
    public void updateUser(User user) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.update(user);
            tx.commit();
        }
    }

    @Override
    public void deleteUser(int id) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.delete(user);
            }
            tx.commit();
        }
    }

    @Override
    public User getUserByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        }
    }

    @Override
    public User authenticateUser(String username, String password) {
        try (Session session = sessionFactory.openSession()) {
            // Try to authenticate by username
            Query<User> query = session.createQuery("FROM User WHERE username = :username AND password = :password", User.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            User user = query.uniqueResult();

            // If not found, try by email
            if (user == null) {
                query = session.createQuery("FROM User WHERE email = :email AND password = :password", User.class);
                query.setParameter("email", username); // username field might contain email
                query.setParameter("password", password);
                user = query.uniqueResult();
            }

            return user;
        }
    }
}
