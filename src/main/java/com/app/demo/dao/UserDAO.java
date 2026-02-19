package com.app.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.app.demo.model.User;

public interface UserDAO extends JpaRepository<User, Long> {
}