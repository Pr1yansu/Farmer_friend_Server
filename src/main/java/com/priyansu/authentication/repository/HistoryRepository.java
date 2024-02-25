package com.priyansu.authentication.repository;

import com.priyansu.authentication.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History,Long> {
}
