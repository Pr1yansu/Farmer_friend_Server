package com.priyansu.authentication.service;

import com.priyansu.authentication.dto.ReqRes;
import com.priyansu.authentication.entity.Role;
import com.priyansu.authentication.entity.User;
import com.priyansu.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailService emailService;

    public ReqRes signUp(ReqRes registrationRequest) {
        ReqRes resp = new ReqRes();
        try {
            User existingUser = userRepository.findByEmail(registrationRequest.getEmail());
            if (existingUser != null) {
                resp.setError("User Already Exists!");
                resp.setStatusCode(400);
                return resp;
            }
            User user = new User();
            user.setName(registrationRequest.getName());
            user.setEmail(registrationRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            User userResult = userRepository.save(user);
            resp.setUser(userResult);
            resp.setMessage("User Saved Successfully!");
            resp.setStatusCode(200);
        } catch (Exception e) {
            e.fillInStackTrace();
            resp.setMessage(e.getMessage());
            resp.setStatusCode(500);
        }
        return resp;
    }

    public ReqRes login(ReqRes loginRequest) {
        ReqRes response = new ReqRes();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            var user = userRepository.findByEmail(loginRequest.getEmail());
            if (user == null) {
                response.setStatusCode(400);
                response.setError("User Not Found!");
                return response;
            }
            var jwt = jwtUtils.generateToken((user));
            var refreshToken = jwtUtils.genrateRefreshToken(new HashMap<>(), user);
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRefreshToken((refreshToken));
            response.setExpirationTime("24Hr");
            response.setMessage("Successfully Signed in!");
        } catch (Exception e) {
            e.fillInStackTrace();
            response.setStatusCode(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public ReqRes forgotPassword(ReqRes reqRes) {
        ReqRes response = new ReqRes();
        User user = userRepository.findByEmail(reqRes.getEmail());
        if (user == null) {
            response.setStatusCode(400);
            response.setError("User Not Found!");
            return response;
        }
        String token = jwtUtils.generateToken(user);
        emailService.sendEmail(user.getEmail(), "Password Reset Link", "http://localhost:3000/reset-password/token=" + token);
        response.setStatusCode(200);
        response.setMessage("Password Reset Link Sent to your Email!");
        return response;
    }

    public ReqRes resetPassword(String token, ReqRes reqRes) {
        ReqRes response = new ReqRes();
        if (reqRes.getPassword() == null || reqRes.getPassword().isEmpty()) {
            response.setStatusCode(400);
            response.setError("Password is Required!");
            return response;
        }
        String email = jwtUtils.extractUsername(token);
        User user = userRepository.findByEmail(email);
        if (user == null) {
            response.setStatusCode(400);
            response.setError("User Not Found!");
            return response;
        }
        user.setPassword(passwordEncoder.encode(reqRes.getPassword()));
        userRepository.save(user);
        response.setStatusCode(200);
        response.setMessage("Password Reset Successfully!");
        return response;
    }
}