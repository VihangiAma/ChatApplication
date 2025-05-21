package org.example.chat_application.dao.impl;

import org.example.chat_application.dao.ChatDAO;
import org.example.chat_application.model.Chat;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class ChatDAOImpl implements ChatDAO {

    private final SessionFactory sessionFactory;

    public ChatDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void saveChat(Chat chat) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(chat);
            tx.commit();
        }
    }

    @Override
    public Chat getChatById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Chat.class, id);
        }
    }

    @Override
    public List<Chat> getAllChats() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Chat", Chat.class).list();
        }
    }

    @Override
    public void updateChat(Chat chat) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.update(chat);
            tx.commit();
        }
    }

    @Override
    public void deleteChat(int id) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Chat chat = session.get(Chat.class, id);
            if (chat != null) {
                session.delete(chat);
            }
            tx.commit();
        }
    }
}
