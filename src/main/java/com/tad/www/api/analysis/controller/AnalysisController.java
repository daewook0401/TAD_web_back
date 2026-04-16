package com.tad.www.api.analysis.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.tad.www.api.analysis.dto.AnalysisDraftUpdateRequest;
import com.tad.www.api.analysis.dto.AnalysisPlayerRecordResponse;
import com.tad.www.api.analysis.dto.AnalysisPlayerRankingResponse;
import com.tad.www.api.analysis.dto.AnalysisRecordSummaryResponse;
import com.tad.www.api.analysis.dto.AnalyzeUploadResponse;
import com.tad.www.api.analysis.service.AnalysisService;
import com.tad.www.api.user.entity.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analyze")
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping(path = {"", "/upload"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalyzeUploadResponse> uploadDraft(
        @AuthenticationPrincipal User currentUser,
        @RequestPart(value = "image", required = false) MultipartFile image,
        @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        MultipartFile targetFile = image != null ? image : file;
        return ResponseEntity.ok(analysisService.uploadDraft(currentUser, targetFile));
    }

    @GetMapping("/my-records")
    public ResponseEntity<List<AnalysisRecordSummaryResponse>> getMyRecords(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(analysisService.getMyRecords(currentUser));
    }

    @GetMapping("/rankings")
    public ResponseEntity<List<AnalysisPlayerRankingResponse>> getPlayerRankings(
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "minGames", required = false) Long minGames,
        @RequestParam(value = "limit", required = false) Integer limit
    ) {
        return ResponseEntity.ok(analysisService.getPlayerRankings(keyword, minGames, limit));
    }

    @GetMapping("/player-records")
    public ResponseEntity<List<AnalysisPlayerRecordResponse>> getPlayerRecords(
        @RequestParam(value = "playerName") String playerName
    ) {
        return ResponseEntity.ok(analysisService.getPlayerRecords(playerName));
    }

    @GetMapping("/records/{gameId}")
    public ResponseEntity<AnalyzeUploadResponse> getConfirmedRecordDetail(@PathVariable Long gameId) {
        return ResponseEntity.ok(analysisService.getConfirmedRecordDetail(gameId));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<AnalyzeUploadResponse> getMyRecordDetail(
        @PathVariable Long gameId,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(analysisService.getMyRecordDetail(gameId, currentUser));
    }

    @PutMapping("/{gameId}/draft")
    public ResponseEntity<AnalyzeUploadResponse> updateDraft(
        @PathVariable Long gameId,
        @AuthenticationPrincipal User currentUser,
        @RequestBody AnalysisDraftUpdateRequest request
    ) {
        return ResponseEntity.ok(analysisService.updateDraft(gameId, currentUser, request));
    }

    @PostMapping("/{gameId}/confirm")
    public ResponseEntity<AnalyzeUploadResponse> confirmDraft(
        @PathVariable Long gameId,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(analysisService.confirmDraft(gameId, currentUser));
    }
}
