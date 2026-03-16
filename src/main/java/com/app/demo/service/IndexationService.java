package com.app.demo.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IndexationService {
    public Map<String, Double> calculateTF(List<String> cleanWords) {
        Map<String, Double> termFrequencies = new HashMap<>();
        double nd = cleanWords.size(); // Total words in this document

        for (String word : cleanWords) {
            termFrequencies.put(word, termFrequencies.getOrDefault(word, 0.0) + 1.0);
        }

        for (String word : termFrequencies.keySet()) {
            termFrequencies.put(word, termFrequencies.get(word) / nd);
        }

        return termFrequencies;
    }
    public List<String> cleanAndTokenize(String rawText) throws IOException {
        List<String> result = new ArrayList<>();
        
        try (Analyzer analyzer = new EnglishAnalyzer();
            TokenStream tokenStream = analyzer.tokenStream("content", rawText)) {
            
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            
            while (tokenStream.incrementToken()) {
                result.add(charTermAttribute.toString());
            }
            
            tokenStream.end();
        }
        return result;
    }
    public String extractTextFromPdf(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}