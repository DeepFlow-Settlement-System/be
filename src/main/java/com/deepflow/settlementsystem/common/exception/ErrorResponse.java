package com.deepflow.settlementsystem.common.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private String message;
    private int status;
}
