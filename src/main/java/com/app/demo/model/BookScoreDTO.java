package com.app.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class BookScoreDTO {
    private Book book;
    private Double score;
}