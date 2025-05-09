package org.example.chat_application.dao.impl;

import org.example.chat_application.dao.MessageDAO;
import org.example.chat_application.model.Message;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class MessageDAOImpl implements MessageDAO {

    private final SessionFactory sessionFactory;

    public MessageDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void saveMessage(Message message) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(message);
            tx.commit();
        }
    }

    @Override
    public Message getMessageById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Message.class, id);
        }
    }

    @Override
    public List<Message> getAllMessages() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Message", Message.class).list();
        }
    }

    @Override
    public List<Message> getMessagesByChatId(int chatId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Message> query = session.createQuery("FROM Message WHERE chat.chatId = :chatId", Message.class);
            query.setParameter("chatId", chatId);
            return query.list();
        }
    }

    @Override
    public void updateMessage(Message message) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.update(message);
            tx.commit();
        }
    }

    @Override
    public void deleteMessage(int id) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Message message = session.get(Message.class, id);
            if (message != null) {
                session.delete(message);
            }
            tx.commit();
        }
    }
}
