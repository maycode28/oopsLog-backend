package com.example.oopsLog.controller;

import com.example.oopsLog.dto.request.AnalysisRequest;
import com.example.oopsLog.dto.response.AnalysisResponse;
import com.example.oopsLog.service.AnalysisService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    // POST /api/analyze — 실패 경험 분석
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyze(@RequestBody AnalysisRequest request) {
        try {
            AnalysisResponse response = analysisService.analyze(request.getText());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET /api/history — 전체 기록 조회
    @GetMapping("/history")
    public ResponseEntity<List<AnalysisResponse>> getHistory() {
        return ResponseEntity.ok(analysisService.getHistory());
    }
}
