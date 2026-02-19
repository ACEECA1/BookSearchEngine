package com.app.demo.service;

import org.springframework.stereotype.Service;
import com.app.demo.dao.UserDAO;
import com.app.demo.model.User;

@Service 
public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User saveUser(User user) {
        return userDAO.save(user);
    }
}