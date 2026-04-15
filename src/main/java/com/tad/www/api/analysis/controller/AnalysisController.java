package com.tad.www.api.analysis.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tad.www.api.analysis.dto.AnalyzeUploadResponse;
import com.tad.www.api.analysis.service.AnalysisService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analyze")
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping(path = {"", "/upload"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalyzeUploadResponse> analyze(
        @RequestPart(value = "image", required = false) MultipartFile image,
        @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        MultipartFile targetFile = image != null ? image : file;
        return ResponseEntity.ok(analysisService.analyzeAndSave(targetFile));
    }
}
