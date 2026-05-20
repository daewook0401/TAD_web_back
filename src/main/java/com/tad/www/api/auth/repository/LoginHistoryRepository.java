package com.tad.www.api.auth.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tad.www.api.auth.entity.LoginHistory;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    long countByUserIdAndLoginResult(Long userId, String loginResult);

    List<LoginHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<LoginHistory> findByUserIdAndLoginResultOrderByCreatedAtDesc(
        Long userId,
        String loginResult,
        Pageable pageable
    );
}
