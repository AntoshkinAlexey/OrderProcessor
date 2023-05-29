package com.example.orderProcessor.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String email;
    private String password_hash;
    private String role;
    private Timestamp create_at;
    private Timestamp updated_at;

    public UserEntity() {
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    public String getRole() {
        return role;
    }

    public void setCreate_at(Timestamp create_at) {
        this.create_at = create_at;
    }

    public void setUpdated_at(Timestamp updated_at) {
        this.updated_at = updated_at;
    }
}
