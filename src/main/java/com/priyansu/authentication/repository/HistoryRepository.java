package com.priyansu.authentication.repository;

import com.priyansu.authentication.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryRepository extends JpaRepository<History,Long> {
    List<History> findByUserId(int userId);
}
