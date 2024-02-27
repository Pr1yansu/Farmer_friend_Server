package com.priyansu.authentication.service;

import com.priyansu.authentication.entity.History;
import com.priyansu.authentication.entity.User;
import com.priyansu.authentication.repository.HistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public List<History> viewHistory(User user){
        try {
            List<History> L_history = new ArrayList<>();
            L_history = historyRepository.findByUserId(user.getId());
            return L_history;
        }catch(Exception e){
            e.fillInStackTrace();
            return null;
        }
    }

    public boolean deleteHistory(History history){
        boolean status = false;
        try {
            historyRepository.deleteById(history.getH_id());
            status = true;
        }catch (Exception e){
            e.fillInStackTrace();
        }
        return status;
    }
}

