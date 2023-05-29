package com.example.orderProcessor.controller;

import com.example.orderProcessor.entity.*;
import com.example.orderProcessor.exception.*;
import com.example.orderProcessor.service.UserService;
import jakarta.security.auth.message.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity registration(@RequestBody UserEntity user) {
        try {
            userService.registration(user);
            return ResponseEntity.ok("Пользователь успешно сохранён");
        } catch (UserAlreadyExistException | UserIncorrectUsernameException | UserIncorrectEmailException |
                 UserIncorrectPasswordException | UserIncorrectRoleException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Произошла ошибка");
        }
    }

    @PostMapping("/signin")
    public ResponseEntity login(@RequestBody UserEntity user) {
        try {
            final JwtResponseEntity token = userService.login(user);
            return ResponseEntity.ok(token);
        } catch (UserIncorrectEmailException | UserIncorrectPasswordException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Произошла ошибка");
        }
    }

    @PostMapping("/token")
    public ResponseEntity getNewAccessToken(@RequestBody RefreshJwtRequestEntity request) {
        final JwtResponseEntity token;
        try {
            token = userService.getAccessToken(request.getRefreshToken());
        } catch (AuthException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh")
    public ResponseEntity getNewRefreshToken(@RequestBody RefreshJwtRequestEntity request) {
        final JwtResponseEntity token;
        try {
            token = userService.refresh(request.getRefreshToken());
        } catch (AuthException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
        return ResponseEntity.ok(token);
    }

    @GetMapping("/user")
    public ResponseEntity getOneUser() {
        final JwtAuthenticationEntity authInfo = userService.getAuthInfo();
        if (authInfo == null) {
            return ResponseEntity.badRequest().body("Некорректный токен");
        }
        return ResponseEntity.ok("Username: " + authInfo.getPrincipal() + "\n" + "Role: " + authInfo.getDetails());
    }
}
