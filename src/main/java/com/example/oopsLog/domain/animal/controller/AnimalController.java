package com.example.oopsLog.domain.animal.controller;

import com.example.oopsLog.common.response.ApiResponse;
import com.example.oopsLog.domain.animal.dto.request.AnimalCreateRequest;
import com.example.oopsLog.domain.animal.dto.request.AnimalUpdateRequest;
import com.example.oopsLog.domain.animal.dto.response.AnimalResponse;
import com.example.oopsLog.domain.animal.service.AnimalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/animals")
public class AnimalController {

    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AnimalResponse>> create(@Valid @RequestBody AnimalCreateRequest request) {
        AnimalResponse response = AnimalResponse.from(animalService.create(request));
        return ResponseEntity.created(URI.create("/api/animals/" + response.animalId()))
                .body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AnimalResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(animalService.findAll().stream().map(AnimalResponse::from).toList()));
    }

    @GetMapping("/{animalId}")
    public ResponseEntity<ApiResponse<AnimalResponse>> findById(@PathVariable Long animalId) {
        return ResponseEntity.ok(ApiResponse.success(AnimalResponse.from(animalService.findById(animalId))));
    }

    @PutMapping("/{animalId}")
    public ResponseEntity<ApiResponse<AnimalResponse>> update(@PathVariable Long animalId, @Valid @RequestBody AnimalUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(AnimalResponse.from(animalService.update(animalId, request))));
    }

    @DeleteMapping("/{animalId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long animalId) {
        animalService.delete(animalId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

