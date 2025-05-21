package org.example.chat_application.dao;

import org.example.chat_application.model.User;
import java.util.List;

public interface UserDAO {
    void saveUser(User user);
    User getUserById(int id);
    List<User> getAllUsers();
    void updateUser(User user);
    void deleteUser(int id);
    User getUserByEmail(String email);//optional
    User authenticateUser(String username, String password);

}
