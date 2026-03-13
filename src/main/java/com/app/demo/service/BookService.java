package com.app.demo.service;
import com.app.demo.dao.BookDAO;
import com.app.demo.model.Book;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;


@Service
public class BookService {
    private final BookDAO bookDAO;
    private final FileStorageService fileStorageService;

    public BookService(BookDAO bookDAO, FileStorageService fileStorageService) {
        this.bookDAO = bookDAO;
        this.fileStorageService = fileStorageService;
    }

    public Book saveBook(String title, String author, MultipartFile file) throws IOException {
        String filePath = fileStorageService.saveFile(file);
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPdfPath(filePath);
        
        return bookDAO.save(book);
    }
}