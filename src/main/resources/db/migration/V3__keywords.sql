CREATE TABLE keywords (
    id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    document_count INT NOT NULL
);

CREATE TABLE book_keywords (
    id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    book_id INT NOT NULL,
    keyword_id INT NOT NULL,
    tf DECIMAL(10, 6) NOT NULL,
    FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE,
    FOREIGN KEY (keyword_id) REFERENCES keywords(id) ON DELETE CASCADE
);