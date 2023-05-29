package com.example.orderProcessor.repository;

import com.example.orderProcessor.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

public interface UserRepo extends CrudRepository<UserEntity, Integer> {

    UserEntity findByUsername(String username);
    UserEntity findByEmail(String email);
}
