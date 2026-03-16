package com.app.demo.service;

import com.app.demo.dao.BookDAO;
import com.app.demo.dao.BookKeywordDAO;
import com.app.demo.dao.KeywordDAO;
import com.app.demo.model.Book;
import com.app.demo.model.BookKeyword;
import com.app.demo.model.Keyword;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class BookService {
    private final BookDAO bookDAO;
    private final FileStorageService fileStorageService;
    private final IndexationService indexationService;
    private final KeywordDAO keywordDAO;
    private final BookKeywordDAO bookKeywordDAO;

    public BookService(BookDAO bookDAO, FileStorageService fileStorageService, 
                       IndexationService indexationService, KeywordDAO keywordDAO, 
                       BookKeywordDAO bookKeywordDAO) {
        this.bookDAO = bookDAO;
        this.fileStorageService = fileStorageService;
        this.indexationService = indexationService;
        this.keywordDAO = keywordDAO;
        this.bookKeywordDAO = bookKeywordDAO;
    }

    @Transactional
    public Book saveBook(String title, String author, MultipartFile file) throws IOException {
        String filePath = fileStorageService.saveFile(file);
        File pdfFile = new File(filePath);
        String textContent = indexationService.extractTextFromPdf(pdfFile);
        List<String> tokens = indexationService.cleanAndTokenize(textContent);
        Map<String, Double> tfMap = indexationService.calculateTF(tokens);
        
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPdfPath(filePath);
        book = bookDAO.save(book);
        // For each keyword, update the global keyword count and create the BookKeyword association
        for (Map.Entry<String, Double> entry : tfMap.entrySet()) {
            String word = entry.getKey();
            Double tfWeight = entry.getValue();

            Keyword keyword = keywordDAO.findByKeyword(word);
            if (keyword == null) {
                keyword = new Keyword();
                keyword.setKeyword(word);
                keyword.setDocumentCount(1);
            } else {
                keyword.setDocumentCount(keyword.getDocumentCount() + 1);
            }
            keyword = keywordDAO.save(keyword);

            BookKeyword bookKeyword = new BookKeyword();
            bookKeyword.setBook(book);
            bookKeyword.setKeyword(keyword);
            bookKeyword.setTf(BigDecimal.valueOf(tfWeight));
            
            bookKeywordDAO.save(bookKeyword);
        }

        System.out.println("Successfully indexed book ID: " + book.getId());
        return book;
    }
}