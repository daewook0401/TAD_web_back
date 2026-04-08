package com.tad.www.api.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tad.www.api.board.entity.BoardCategory;

@Repository
public interface BoardCategoryRepository extends JpaRepository<BoardCategory, Long> {
    List<BoardCategory> findAllByOrderByDisplayOrderAscIdAsc();
    Optional<BoardCategory> findByCategoryKey(String categoryKey);
}
