package com.priyansu.authentication.controllers;

import com.priyansu.authentication.config.CloudinaryConfig;
import com.priyansu.authentication.dto.ReqRes;
import com.priyansu.authentication.entity.User;
import com.priyansu.authentication.service.AuthService;
import com.priyansu.authentication.service.JwtUtils;
import com.priyansu.authentication.service.UserServices;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/auth/users")
public class UserController {

    @Autowired
    private UserServices userServices;

    @Autowired
    private CloudinaryConfig cloudinaryConfig;

    @Autowired
    private JwtUtils jwtTokenProvider;

    @Autowired
    AuthService authService;
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return new ResponseEntity<>("Hello from GET route!", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<ReqRes> login(@RequestBody ReqRes loginReq) {
        return ResponseEntity.ok(authService.login(loginReq));
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
            String username = jwtTokenProvider.extractUsername(token);
            User user = userServices.loadUserByUsername(username);

            if (user == null) {
                return new ResponseEntity<>("You are not authorized to upload an image", HttpStatus.UNAUTHORIZED);
            }

            if (file != null && !file.isEmpty()) {
                Map uploadResult = cloudinaryConfig.uploadAvatar(file.getBytes());
                String imageUrl = uploadResult.get("url").toString();

                String oldImageUrl = user.getImage();
                if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                    cloudinaryConfig.deleteImage(oldImageUrl);
                }

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
    public ResponseEntity<ReqRes> registerUser(@RequestBody ReqRes registerReq) {
       return ResponseEntity.ok(authService.signUp(registerReq));
    }

    @GetMapping("/profile")
    public ResponseEntity getProfile(HttpServletRequest request) {
        String token = jwtTokenProvider.extractTokenFromHeader(request);
        if (token == null) {
            return new ResponseEntity<>(
                    "You are not authorized to view this profile"
                    , HttpStatus.UNAUTHORIZED);
        }
        String username = jwtTokenProvider.extractUsername(token);
        User user = userServices.loadUserByUsername(username);
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
        String username = jwtTokenProvider.extractUsername(token);
        User authenticatedUser = userServices.loadUserByUsername(username);
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
