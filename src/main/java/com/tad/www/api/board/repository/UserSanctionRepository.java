package com.tad.www.api.board.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tad.www.api.board.entity.UserSanction;

@Repository
public interface UserSanctionRepository extends JpaRepository<UserSanction, Long> {

    @EntityGraph(attributePaths = {"user", "createdBy", "revokedBy"})
    @Query("""
        select s
        from UserSanction s
        where s.user.id = :userId
          and s.revokedAt is null
          and s.startsAt <= :now
          and (s.expiresAt is null or s.expiresAt > :now)
        order by s.createdAt desc, s.id desc
        """)
    List<UserSanction> findActiveByUserId(
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "createdBy", "revokedBy"})
    @Query("""
        select s
        from UserSanction s
        where (:userId is null or s.user.id = :userId)
          and (
            :activeOnly = false
            or (
              s.revokedAt is null
              and s.startsAt <= :now
              and (s.expiresAt is null or s.expiresAt > :now)
            )
          )
        order by s.createdAt desc, s.id desc
        """)
    List<UserSanction> findAdminSanctions(
        @Param("userId") Long userId,
        @Param("activeOnly") boolean activeOnly,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );
}
