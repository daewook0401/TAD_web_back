package com.tad.www.api.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public record AnalysisServiceRequest(
    String bucket,
    @JsonProperty("object_key") String objectKey
) {
}
