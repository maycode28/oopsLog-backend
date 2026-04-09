// FailureListResponse.java — 목록용
package com.example.oopsLog.domain.analysis.dto.response;

import com.example.oopsLog.domain.analysis.entity.Failure;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FailureListResponse {

    private final Long failureId;
    private final String content;
    private final String title;          // AiCorrection.title (없으면 null)
    private final LocalDateTime createdAt;

    public FailureListResponse(Failure failure) {
        this.failureId = failure.getFailureId();
        this.content = failure.getContent();
        this.title = failure.getAiCorrection() != null
                ? failure.getAiCorrection().getTitle()
                : null;
        this.createdAt = failure.getCreatedAt();
    }
}