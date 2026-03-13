package com.app.demo.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data // Generates getters, setters, toString, etc.
@NoArgsConstructor // Generates the default constructor Jackson needs! 🔑
@AllArgsConstructor
public class UserDTO {
    private String username;
    private String email;
    private String password;

    public UserDTO(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
    }
}
