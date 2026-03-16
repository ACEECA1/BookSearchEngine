package com.app.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "keywords")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Keyword {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String keyword;

    @Column(nullable = false)
    private Integer documentCount;
}