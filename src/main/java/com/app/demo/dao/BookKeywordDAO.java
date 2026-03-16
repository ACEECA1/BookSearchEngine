package com.app.demo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.app.demo.model.BookKeyword;
import com.app.demo.model.Keyword;
import com.app.demo.model.Book;

public interface BookKeywordDAO extends JpaRepository<BookKeyword, Integer> {
    public List<BookKeyword> findByKeyword(Keyword keyword);
    public List<BookKeyword> findByBook(Book book);
}