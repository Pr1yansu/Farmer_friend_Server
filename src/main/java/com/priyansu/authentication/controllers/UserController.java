package com.priyansu.authentication.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.authentication.config.CloudinaryConfig;
import com.priyansu.authentication.config.JwtTokenProvider;
import com.priyansu.authentication.entity.User;
import com.priyansu.authentication.service.UserServices;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserServices userServices;

    @Autowired
    private CloudinaryConfig cloudinaryConfig;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return new ResponseEntity<>("Hello from GET route!", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User credentials, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null) {
            return new ResponseEntity<>("You are already logged in", HttpStatus.OK);
        }
        User user = userServices.authenticateUser(credentials.getEmail(), credentials.getPassword());
        if (user != null) {
            String newToken = jwtTokenProvider.generateToken(user);
            return new ResponseEntity<>(newToken, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid credentials", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            SecurityContextHolder.clearContext();
            return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("You are not logged in", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            String token = jwtTokenProvider.extractTokenFromHeader(request);
            if (!jwtTokenProvider.isTokenValid(token)) {
                return new ResponseEntity<>("Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }
            User user = jwtTokenProvider.extractUserFromToken(token);
            if (user == null) {
                return new ResponseEntity<>("You are not authorized to upload image", HttpStatus.UNAUTHORIZED);
            }
            if (file != null && !file.isEmpty()) {
                Map uploadResult = cloudinaryConfig.uploadAvatar(file.getBytes());
                String imageUrl = uploadResult.get("url").toString();
                boolean isUpdated = userServices.updateUserImage(user.getEmail(), imageUrl);
                if (isUpdated) {
                    return new ResponseEntity<>("Image uploaded successfully", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("Image upload failed", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>("File is empty or null", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register")
    public ResponseEntity registerUser(@RequestBody User user) {
        try {
            User createdUser = userServices.createUser(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            e.fillInStackTrace();
            return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity getProfile(HttpServletRequest request) {
        String token = jwtTokenProvider.extractTokenFromHeader(request);
        if (token == null) {
            return new ResponseEntity<>(
                    "You are not authorized to view this profile"
                    , HttpStatus.UNAUTHORIZED);
        }
        User user = jwtTokenProvider.extractUserFromToken(token);
        if (user == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        User profile = userServices.profile(user.getEmail());
        if (profile != null) {
            return new ResponseEntity<>(profile, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateUser(@RequestBody User user, HttpServletRequest request) {
        String token = jwtTokenProvider.extractTokenFromHeader(request);
        if (token == null) {
            return new ResponseEntity<>("You are not authorized to update this user", HttpStatus.UNAUTHORIZED);
        }
        User authenticatedUser = jwtTokenProvider.extractUserFromToken(token);
        if (authenticatedUser == null) {
            return new ResponseEntity<>("You are not authorized to update this user", HttpStatus.UNAUTHORIZED);
        }
        if (!authenticatedUser.getEmail().equals(user.getEmail())) {
            return new ResponseEntity<>("You are not authorized to update this user", HttpStatus.UNAUTHORIZED);
        }
        try {
            User updatedUser = userServices.updateUser(user);
            if (updatedUser != null) {
                return new ResponseEntity<>("User updated successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
