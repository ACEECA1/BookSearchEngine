package com.app.demo.controller;

import org.eclipse.jetty.http.HttpStatus;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.app.demo.dao.BookDAO;
import com.app.demo.model.Book;
import com.app.demo.model.BookScoreDTO;
import com.app.demo.model.ResponseDTO;
import com.app.demo.service.BookService;
import com.app.demo.service.SearchService;

import org.springframework.core.io.Resource;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final BookDAO bookDAO;
    private final SearchService searchService;
    public BookController(BookService bookService, BookDAO bookDAO, SearchService searchService) {
        this.bookService = bookService;
        this.bookDAO = bookDAO;
        this.searchService = searchService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseDTO> uploadBook(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("author") String author) {
        
        try {
            bookService.saveBook(title, author, file);
            ResponseDTO responseDTO = new ResponseDTO("Book uploaded successfully");
            responseDTO.setStatus(HttpStatus.CREATED_201);
            return ResponseEntity.ok(responseDTO);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(new ResponseDTO("Error: " + e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<ResponseDTO> getBooks() {
        List<Book> books = bookDAO.findAll();
        ResponseDTO responseDTO = new ResponseDTO(books);
        responseDTO.setStatus(HttpStatus.OK_200);
        return ResponseEntity.ok(responseDTO);
    }

   @GetMapping("/upload/**")
    public ResponseEntity<Resource> getBookPdf(HttpServletRequest request) {
        try {
            String requestURL = request.getRequestURL().toString();
            String path = requestURL.split("/api/books/upload/")[1];
            String cleanPath = URLDecoder.decode(path, StandardCharsets.UTF_8);

            if (cleanPath.contains("..")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN_403).build();
            }
            Path basePath = Paths.get("uploads/books").toAbsolutePath().normalize();
            Path filePath = basePath.resolve(cleanPath).normalize();

            if (!filePath.startsWith(basePath)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN_403).build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = request.getServletContext().getMimeType(filePath.toString());
                if (contentType == null) {
                    contentType = "application/pdf"; 
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND_404).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR_500).build();
        }
    }
    @GetMapping("/search")
    public ResponseEntity<ResponseDTO> search(@RequestParam("q") String query) {
        try {
            List<BookScoreDTO> results = searchService.search(query);
            ResponseDTO response = new ResponseDTO(results);
            response.setStatus(HttpStatus.OK_200);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(new ResponseDTO("Search failed: " + e.getMessage()));
        }
    }
}