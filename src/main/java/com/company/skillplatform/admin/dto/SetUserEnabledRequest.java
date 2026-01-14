package com.company.skillplatform.admin.dto;

import jakarta.validation.constraints.NotNull;

public record SetUserEnabledRequest(
        @NotNull Boolean enabled
) {}
