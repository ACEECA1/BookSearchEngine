package com.app.demo.service;

import com.app.demo.dao.BookDAO;
import com.app.demo.dao.KeywordDAO;
import com.app.demo.dao.BookKeywordDAO;
import com.app.demo.model.Book;
import com.app.demo.model.BookKeyword;
import com.app.demo.model.BookScoreDTO;
import com.app.demo.model.Keyword;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private final IndexationService indexationService;
    private final KeywordDAO keywordDAO;
    private final BookKeywordDAO bookKeywordDAO;
    private final BookDAO bookDAO;

    public SearchService(IndexationService indexationService, KeywordDAO keywordDAO, BookDAO bookDAO , BookKeywordDAO bookKeywordDAO) {
        this.indexationService = indexationService;
        this.keywordDAO = keywordDAO;
        this.bookDAO = bookDAO;
        this.bookKeywordDAO = bookKeywordDAO;
    }

    public List<BookScoreDTO> search(String rawQuery) throws IOException {
        long totalBooks = bookDAO.count();
        if (totalBooks == 0) {
            System.out.println("The library is empty!");
            return new ArrayList<>();
        }

        List<String> queryTokens = indexationService.cleanAndTokenize(rawQuery);
        Map<String, Double> queryTf = indexationService.calculateTF(queryTokens);
        Map<String, Double> queryVector = new HashMap<>();

        // Calculate TF-IDF for the query
        for (Map.Entry<String, Double> entry : queryTf.entrySet()) {
            String word = entry.getKey();
            Double tf = entry.getValue();

            Keyword keywordObj = keywordDAO.findByKeyword(word);

            if (keywordObj != null) {
                double df = keywordObj.getDocumentCount();
                double idf = Math.log((double) totalBooks / df);

                double tfIdf = tf * idf;
                queryVector.put(word, tfIdf);
            }
        }

        System.out.println("Query Vector Math Complete: " + queryVector);
        // Calculate cosine similarity first half: dot product of query vector and each book vector
        Map<Book, Double> dotProducts = new HashMap<>();

        for (String word : queryVector.keySet()) {
            Keyword kw = keywordDAO.findByKeyword(word);
            double qWeight = queryVector.get(word);
            double idf = Math.log((double) totalBooks / kw.getDocumentCount());

            List<BookKeyword> occurrences = bookKeywordDAO.findByKeyword(kw);

            for (BookKeyword bk : occurrences) {
                Book book = bk.getBook();
                double dWeight = bk.getTf().doubleValue() * idf;
                dotProducts.put(book, dotProducts.getOrDefault(book, 0.0) + (qWeight * dWeight));
            }
        }

        double queryMagnitude = Math.sqrt(queryVector.values().stream()
                .mapToDouble(v -> v * v).sum());
        // Calculate cosine similarity second half: divide dot product by magnitude of query and magnitude of book vector
        List<BookScoreDTO> results = new ArrayList<>();

        for (Book book : dotProducts.keySet()) {
            double dotProduct = dotProducts.get(book);

            double bookMagnitude = calculateBookMagnitude(book, totalBooks);

            double score = dotProduct / (queryMagnitude * bookMagnitude);
            results.add(new BookScoreDTO(book, score));
        }

        results.sort((a, b) -> b.getScore().compareTo(a.getScore()));
        return results;
    }

    private double calculateBookMagnitude(Book book, long totalBooks) {
        List<BookKeyword> allWords = bookKeywordDAO.findByBook(book); // Get all keywords for this book
        double sumSquares = 0.0;
        for (BookKeyword bk : allWords) {
            double idf = Math.log((double) totalBooks / bk.getKeyword().getDocumentCount());
            double tfIdf = bk.getTf().doubleValue() * idf;
            sumSquares += (tfIdf * tfIdf); // Sqrt(tfIdf^2)
        }
        return Math.sqrt(sumSquares); // Return the magnitude of the book vector
    }
}