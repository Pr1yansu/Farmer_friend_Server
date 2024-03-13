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

    public ReqRes signUp(ReqRes registrationRequest){
        ReqRes resp = new ReqRes();
        try{
            User user = new User();
            user.setName(registrationRequest.getName());
            user.setEmail(registrationRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            User userResult = userRepository.save(user);
            resp.setUser(userResult);
            resp.setMessage("User Saved Successfully!");
            resp.setStatusCode(200);
        }
        catch (Exception e){
            e.fillInStackTrace();
            resp.setMessage(e.getMessage());
            resp.setStatusCode(500);
        }
        return resp;
    }
    public ReqRes login(ReqRes loginRequest){
        ReqRes response = new ReqRes();
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword()));
            var user = userRepository.findByEmail(loginRequest.getEmail());
            System.out.println("User is: "+user);
            var jwt = jwtUtils.genrateToken((user));
            var refreashToken = jwtUtils.genrateRefreashToken(new HashMap<>(),user);
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRefreshToken((refreashToken));
            response.setExpirationTime("24Hr");
            response.setMessage("Successfully Signed in!");
        }
        catch(Exception e){
            e.fillInStackTrace();
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public ReqRes refreashToken(ReqRes refreashTokenRequest){
        ReqRes response = new ReqRes();
        String email =jwtUtils.extractUsername(refreashTokenRequest.getToken());
        User user = userRepository.findByEmail(email);
        if(jwtUtils.isTokenValid(refreashTokenRequest.getToken(),user)){
            var jwt = jwtUtils.genrateToken(user);
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRefreshToken(refreashTokenRequest.getToken());
            response.setExpirationTime("24hr");
            response.setMessage("Successfully Refreashed Token");
        }
        response.setStatusCode(500);
        return response;
    }





}
