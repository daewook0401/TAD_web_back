package com.tad.www.api.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.tad.www.api.board.dto.BoardAttachmentResponse;
import com.tad.www.api.board.entity.BoardPost;
import com.tad.www.api.board.entity.BoardPostAttachment;
import com.tad.www.api.board.repository.BoardCommentAttachmentRepository;
import com.tad.www.api.board.repository.BoardPostAttachmentRepository;
import com.tad.www.core.config.minio.MinioStorageService;

@ExtendWith(MockitoExtension.class)
class BoardAttachmentServiceTest {

    @Mock
    private MinioStorageService minioStorageService;

    @Mock
    private BoardPostAttachmentRepository boardPostAttachmentRepository;

    @Mock
    private BoardCommentAttachmentRepository boardCommentAttachmentRepository;

    @InjectMocks
    private BoardAttachmentService boardAttachmentService;

    @Test
    void storePostAttachmentsPersistsObjectLocatorAndReturnsDrivePublicUrl() {
        BoardPost post = BoardPost.builder().id(2L).build();
        MockMultipartFile file = new MockMultipartFile("files", "preview.png", "image/png", new byte[] {1, 2, 3});
        MinioStorageService.StoredObject storedObject = new MinioStorageService.StoredObject(
            "tad",
            "board/posts/2/preview.png",
            "https://drive.example.com/public/tad/board/posts/2/preview.png",
            "preview.png",
            "preview.png",
            "image/png",
            3L
        );
        when(minioStorageService.store(eq(file), eq("board/posts/2"))).thenReturn(storedObject);
        when(minioStorageService.normalizeContentType("image/png", "preview.png")).thenReturn("image/png");
        when(minioStorageService.buildPublicFileUrl("tad", "board/posts/2/preview.png"))
            .thenReturn("https://drive.example.com/public/tad/board/posts/2/preview.png");
        when(boardPostAttachmentRepository.save(any(BoardPostAttachment.class))).thenAnswer(invocation -> {
            BoardPostAttachment attachment = invocation.getArgument(0);
            attachment.setId(10L);
            return attachment;
        });

        List<BoardAttachmentResponse> responses = boardAttachmentService.storePostAttachments(post, List.of(file));

        ArgumentCaptor<BoardPostAttachment> attachmentCaptor = ArgumentCaptor.forClass(BoardPostAttachment.class);
        verify(boardPostAttachmentRepository).save(attachmentCaptor.capture());
        assertThat(attachmentCaptor.getValue().getBucket()).isEqualTo("tad");
        assertThat(attachmentCaptor.getValue().getObjectKey()).isEqualTo("board/posts/2/preview.png");
        assertThat(responses).singleElement().satisfies(response -> {
            assertThat(response.getBucket()).isEqualTo("tad");
            assertThat(response.getObjectKey()).isEqualTo("board/posts/2/preview.png");
            assertThat(response.getFileUrl()).isEqualTo("https://drive.example.com/public/tad/board/posts/2/preview.png");
        });
    }
}
