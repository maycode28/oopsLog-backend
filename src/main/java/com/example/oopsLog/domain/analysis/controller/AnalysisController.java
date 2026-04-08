package com.example.oopsLog.domain.analysis.controller;

import com.example.oopsLog.common.response.ApiResponse;
import com.example.oopsLog.domain.analysis.dto.request.AnalysisCreateRequest;
import com.example.oopsLog.domain.analysis.dto.request.AnalysisUpdateRequest;
import com.example.oopsLog.domain.analysis.dto.response.AnalysisResponse;
import com.example.oopsLog.domain.analysis.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/analyses")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AnalysisResponse>> create(@Valid @RequestBody AnalysisCreateRequest request) {
        AnalysisResponse response = AnalysisResponse.from(analysisService.create(request));
        return ResponseEntity.created(URI.create("/api/analyses/" + response.correctionId()))
                .body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AnalysisResponse>>> findAll() {
        List<AnalysisResponse> responses = analysisService.findAll().stream().map(AnalysisResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{correctionId}")
    public ResponseEntity<ApiResponse<AnalysisResponse>> findById(@PathVariable Long correctionId) {
        return ResponseEntity.ok(ApiResponse.success(AnalysisResponse.from(analysisService.findById(correctionId))));
    }

    @PutMapping("/{correctionId}")
    public ResponseEntity<ApiResponse<AnalysisResponse>> update(
            @PathVariable Long correctionId,
            @Valid @RequestBody AnalysisUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(AnalysisResponse.from(analysisService.update(correctionId, request))));
    }

    @DeleteMapping("/{correctionId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long correctionId) {
        analysisService.delete(correctionId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

