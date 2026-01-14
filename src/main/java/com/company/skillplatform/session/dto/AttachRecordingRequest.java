package com.company.skillplatform.session.dto;

import jakarta.validation.constraints.NotBlank;

public record AttachRecordingRequest(
        @NotBlank String recordingUrl
) {}

