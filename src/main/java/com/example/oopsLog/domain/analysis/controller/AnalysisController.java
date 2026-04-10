package com.example.oopsLog.domain.analysis.controller;

import com.example.oopsLog.common.response.ApiResponse;
import com.example.oopsLog.domain.analysis.dto.request.AnalysisRequest;
import com.example.oopsLog.domain.analysis.dto.response.AnalysisResponse;
import com.example.oopsLog.domain.analysis.dto.response.FailureDetailResponse;
import com.example.oopsLog.domain.analysis.dto.response.FailureListResponse;
import com.example.oopsLog.domain.analysis.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analyses/{userId}")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    // POST /api/analyses/{userId}/analyze
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<AnalysisResponse>> analyze(
            @PathVariable Long userId,
            @RequestBody AnalysisRequest request) {

        AnalysisResponse response = analysisService.analyze(userId, request.getText());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // GET /api/analyses/{userId}/failures
    @GetMapping("/failures")
    public ResponseEntity<ApiResponse<List<FailureListResponse>>> getFailureList(
            @PathVariable Long userId) {

        List<FailureListResponse> response = analysisService.getFailureList(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // GET /api/analyses/{userId}/failures/{failureId}
    @GetMapping("/failures/{failureId}")
    public ResponseEntity<ApiResponse<FailureDetailResponse>> getFailureDetail(
            @PathVariable Long userId,
            @PathVariable Long failureId) {

        FailureDetailResponse response = analysisService.getFailureDetail(userId, failureId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}