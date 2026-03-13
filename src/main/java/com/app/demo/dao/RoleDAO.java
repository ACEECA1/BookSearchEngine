package com.app.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.app.demo.model.Role;

public interface RoleDAO extends JpaRepository<Role, Integer> {
    Role findByName(String name);
}