package com.tad.www.api.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tad.www.api.board.entity.BoardCommentAttachment;

@Repository
public interface BoardCommentAttachmentRepository extends JpaRepository<BoardCommentAttachment, Long> {

    List<BoardCommentAttachment> findByCommentIdOrderBySortOrderAscIdAsc(Long commentId);
}
