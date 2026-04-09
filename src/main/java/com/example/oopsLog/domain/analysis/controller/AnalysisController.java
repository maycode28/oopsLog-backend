package com.example.oopsLog.domain.analysis.controller;

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
    public ResponseEntity<AnalysisResponse> analyze(
            @PathVariable Long userId,
            @RequestBody AnalysisRequest request) {
        try {
            return ResponseEntity.ok(analysisService.analyze(userId, request.getText()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET /api/analyses/{userId}/failures
    @GetMapping("/failures")
    public ResponseEntity<List<FailureListResponse>> getFailureList(@PathVariable Long userId) {
        return ResponseEntity.ok(analysisService.getFailureList(userId));
    }

    // GET /api/analyses/{userId}/failures/{failureId}
    @GetMapping("/failures/{failureId}")
    public ResponseEntity<FailureDetailResponse> getFailureDetail(
            @PathVariable Long userId,
            @PathVariable Long failureId) {
        return ResponseEntity.ok(analysisService.getFailureDetail(userId, failureId));
    }
}