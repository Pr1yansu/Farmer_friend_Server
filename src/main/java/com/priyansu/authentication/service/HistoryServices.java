package com.priyansu.authentication.service;

import com.priyansu.authentication.entity.History;
import com.priyansu.authentication.entity.User;
import com.priyansu.authentication.repository.HistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HistoryServices{
    @Autowired
    private HistoryRepository historyRepository;

    public History createHistory(History history, User user){
        try {
            history.setUser(user);
            return historyRepository.save(history);
        }catch (Exception e){
            e.fillInStackTrace();
            return null;
        }
    }
}

