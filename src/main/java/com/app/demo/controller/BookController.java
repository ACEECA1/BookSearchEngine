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
import com.app.demo.model.ResponseDTO;
import com.app.demo.service.FileStorageService;

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

    private final BookDAO bookDAO;
    private final FileStorageService fileStorageService;

    public BookController(BookDAO bookDAO, FileStorageService fileStorageService) {
        this.bookDAO = bookDAO;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseDTO> uploadBook(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("author") String author) {
        
        try {
            String path = fileStorageService.saveFile(file);

            Book book = new Book();
            book.setTitle(title);
            book.setAuthor(author);
            book.setPdfPath(path);
            bookDAO.save(book);
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
            // 1. Extract the file name/path after "/api/books/upload/"
            String requestURL = request.getRequestURL().toString();
            String path = requestURL.split("/api/books/upload/")[1];
            String cleanPath = URLDecoder.decode(path, StandardCharsets.UTF_8);

            // 2. Security: Prevent Path Traversal (e.g., trying to read ../../../etc/passwd)
            if (cleanPath.contains("..")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN_403).build();
            }

            // 3. Resolve the exact file location on the server
            Path basePath = Paths.get("uploads/books").toAbsolutePath().normalize();
            Path filePath = basePath.resolve(cleanPath).normalize();

            // Double-check that the resolved path is strictly inside the base directory
            if (!filePath.startsWith(basePath)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN_403).build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            // 4. If the file exists, serve it
            if (resource.exists() && resource.isReadable()) {
                // Try to detect content type, default to PDF if unknown
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
    
}