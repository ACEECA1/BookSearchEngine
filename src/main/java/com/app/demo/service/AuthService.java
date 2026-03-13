package com.app.demo.service;

import java.util.HashSet;

import org.springframework.stereotype.Service;

import com.app.demo.dao.RoleDAO;
import com.app.demo.dao.UserDAO;
import com.app.demo.model.User;
import com.app.demo.model.UserDTO;
import com.app.demo.model.Role;


@Service
public class AuthService {

    private final UserDAO userDAO;
    private final RoleDAO roleDAO;

    public AuthService(UserDAO userDAO, RoleDAO roleDAO) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
    }
    public User registerUser(User user) {
        Role userRole = roleDAO.findByName("USER");
        if (userRole == null) {
            userRole = new Role();
            userRole.setName("USER");
            roleDAO.save(userRole);
        }
        user.setRoles(new HashSet<>());
        user.getRoles().add(userRole);
        return userDAO.save(user);
    }
    public User registerUser(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        return registerUser(user);
    }
    public User authenticate(String email, String password) {
        User user = userDAO.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
    public User getUserById(Integer userId) {
        return userDAO.findById(userId).orElse(null);
    }
    public User getUserByEmail(String email) {
        return userDAO.findByEmail(email);
    }
}