package com.tad.www.api.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tad.www.api.board.entity.BoardPostAttachment;

@Repository
public interface BoardPostAttachmentRepository extends JpaRepository<BoardPostAttachment, Long> {

    List<BoardPostAttachment> findByPostIdOrderBySortOrderAscIdAsc(Long postId);
}
