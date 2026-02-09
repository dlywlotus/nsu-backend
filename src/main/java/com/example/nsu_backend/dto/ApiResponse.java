package com.example.nsu_backend.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ApiResponse<T> {
    private final LocalDateTime timestamp;
    private final T data;
    private final List<ErrorDetail> errors;

    public ApiResponse(T data) {
        this.timestamp = LocalDateTime.now();
        this.data = data;
        this.errors = null;
    }

    public ApiResponse(ErrorDetail error) {
        this.timestamp = LocalDateTime.now();
        this.data = null;
        this.errors = List.of(error);
    }

    public ApiResponse(List<ErrorDetail> errors) {
        this.timestamp = LocalDateTime.now();
        this.data = null;
        this.errors = errors;
    }
}