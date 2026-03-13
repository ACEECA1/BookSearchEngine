package com.app.demo;


import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.app.demo.dao.UserDAO;
import com.app.demo.model.User;
import com.app.demo.service.UserService;

@RestController
public class HelloController {
    private final UserDAO userDAO;
    private final UserService userService;
    public HelloController(UserDAO userDAO, UserService userService) {
        this.userDAO = userDAO;
        this.userService = userService;
    }
    @GetMapping("/hello")
    public String sayHello(@RequestParam String param) { //same name as the query parameter
        return "Hello get!" + param;
    }
    @PostMapping("/hello")
    public String sayHelloPost() {  
        return "Hello post";
    }
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }
    @PostMapping("/file")
    public String createFile(@RequestParam MultipartFile file) { // same as the form field name
        return "Received and a file named: " + file.getOriginalFilename() + " with size: " + file.getSize() + " bytes";
    }
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }
}
