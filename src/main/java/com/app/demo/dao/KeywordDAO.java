package com.app.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.app.demo.model.Keyword;

public interface KeywordDAO extends JpaRepository<Keyword, Integer> {
    Keyword findByKeyword(String keyword);
}
