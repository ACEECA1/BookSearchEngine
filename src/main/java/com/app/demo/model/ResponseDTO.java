package com.app.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResponseDTO {
    private int status;
    private Object message; 
    public ResponseDTO(Object message) {
        this.message = message;
    }
}
