package com.app.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "roles") 
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Role {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    Integer id;
    String name;
}
