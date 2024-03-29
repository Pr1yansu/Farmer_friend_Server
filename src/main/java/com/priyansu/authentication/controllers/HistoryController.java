package com.priyansu.authentication.controllers;

import com.priyansu.authentication.entity.History;
import com.priyansu.authentication.entity.User;
import com.priyansu.authentication.service.HistoryServices;
import com.priyansu.authentication.service.JwtUtils;
import com.priyansu.authentication.service.UserServices;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private HistoryServices historyServices;

    @Autowired
    private JwtUtils jwtTokenProvider;

    @Autowired
    UserServices userServices;

    @GetMapping("/test")
    public ResponseEntity<String> testingHistory() {
        return new ResponseEntity<>("History ok", HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity create(@RequestBody History history, HttpServletRequest request, Principal principal) {
        String token = jwtTokenProvider.extractTokenFromHeader(request);
        if (token == null) {
            return new ResponseEntity<>("You are not authorized", HttpStatus.UNAUTHORIZED);
        }
        String username = jwtTokenProvider.extractUsername(token);
        User authenticatedUser = userServices.loadUserByUsername(username);
        if (authenticatedUser == null) {
            return new ResponseEntity<>("You are not authorized user", HttpStatus.UNAUTHORIZED);
        }
        try {
            History Created_history = historyServices.createHistory(history, authenticatedUser);
            return new ResponseEntity("History is saved", HttpStatus.OK);
        } catch (Exception e) {
            e.fillInStackTrace();
            return new ResponseEntity("Problem to create history", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/view")
    public ResponseEntity viewHistory(HttpServletRequest request) {
        String token = jwtTokenProvider.extractTokenFromHeader(request);
        if (token == null) {
            return new ResponseEntity<>("You are not authorized", HttpStatus.UNAUTHORIZED);
        }
        String username = jwtTokenProvider.extractUsername(token);
        User authenticatedUser = userServices.loadUserByUsername(username);
        if (authenticatedUser == null) {
            return new ResponseEntity<>("You are not authorized user", HttpStatus.UNAUTHORIZED);
        }
        try {
            List<History> L_History = historyServices.viewHistory(authenticatedUser);
            return new ResponseEntity(L_History, HttpStatus.OK);
        } catch (Exception e) {
            e.fillInStackTrace();
            return new ResponseEntity("Problem to view history list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity deleteHistory(@RequestBody History history) {
        boolean status;
        try {
            status = historyServices.deleteHistory(history);
            if (status) {
                return new ResponseEntity<>("History deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Error occurred to delete history", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            return new ResponseEntity<>("Problem to delete history", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
