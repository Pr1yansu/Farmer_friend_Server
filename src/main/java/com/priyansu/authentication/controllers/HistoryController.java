package com.priyansu.authentication.controllers;

import com.priyansu.authentication.config.JwtTokenProvider;
import com.priyansu.authentication.entity.History;
import com.priyansu.authentication.entity.User;
import com.priyansu.authentication.service.HistoryServices;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private HistoryServices historyServices;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/test")
    public ResponseEntity<String> testingHistory(){
        return new ResponseEntity<>("History ok", HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity create(@RequestBody History history, HttpServletRequest request){
        String token = jwtTokenProvider.extractTokenFromHeader(request);
        if (token == null) {
            return new ResponseEntity<>("You are not authorized", HttpStatus.UNAUTHORIZED);
        }
        User authenticatedUser = jwtTokenProvider.extractUserFromToken(token);
        if (authenticatedUser == null) {
            return new ResponseEntity<>("You are not authorized user", HttpStatus.UNAUTHORIZED);
        }
        try {
            History Created_history = historyServices.createHistory(history,authenticatedUser);
            return new ResponseEntity(Created_history,HttpStatus.OK);
        }catch (Exception e){
            e.fillInStackTrace();
            return new ResponseEntity("Problem to create history",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
