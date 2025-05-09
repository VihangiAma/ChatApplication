package org.example.chat_application.dao.impl;

import org.example.chat_application.dao.SubscriptionDAO;
import org.example.chat_application.model.Subscription;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class SubscriptionDAOImpl implements SubscriptionDAO {

    private final SessionFactory sessionFactory;

    public SubscriptionDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void saveSubscription(Subscription subscription) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(subscription);
            tx.commit();
        }
    }

    @Override
    public Subscription getSubscriptionById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Subscription.class, id);
        }
    }

    @Override
    public List<Subscription> getSubscriptionsByUserId(int userId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Subscription> query = session.createQuery(
                "FROM Subscription s WHERE s.user.user_id = :userId", 
                Subscription.class);
            query.setParameter("userId", userId);
            return query.list();
        }
    }

    @Override
    public List<Subscription> getSubscriptionsByChatId(int chatId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Subscription> query = session.createQuery(
                "FROM Subscription s WHERE s.chat.chatId = :chatId", 
                Subscription.class);
            query.setParameter("chatId", chatId);
            return query.list();
        }
    }

    @Override
    public Subscription getSubscriptionByUserAndChat(int userId, int chatId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Subscription> query = session.createQuery(
                "FROM Subscription s WHERE s.user.user_id = :userId AND s.chat.chatId = :chatId", 
                Subscription.class);
            query.setParameter("userId", userId);
            query.setParameter("chatId", chatId);
            return query.uniqueResult();
        }
    }

    @Override
    public void deleteSubscription(Subscription subscription) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.delete(subscription);
            tx.commit();
        }
    }

    @Override
    public boolean deleteSubscriptionByUserAndChat(int userId, int chatId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            Subscription subscription = getSubscriptionByUserAndChat(userId, chatId);
            if (subscription != null) {
                session.delete(subscription);
                tx.commit();
                return true;
            }

            tx.commit();
            return false;
        }
    }
}
