package com.mediaalterations.storageservice.dto;

import lombok.Builder;
import lombok.Data;

@Builder
public record ApiError(
   int status,
   String errorMessage,
   String errorClass
) {}
