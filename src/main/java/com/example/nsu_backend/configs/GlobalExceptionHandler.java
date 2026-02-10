package com.example.nsu_backend.configs;

import com.example.nsu_backend.dto.ApiResponse;
import com.example.nsu_backend.dto.ErrorDetail;
import com.example.nsu_backend.errorCodes.ApiErrorCode;
import com.example.nsu_backend.errorCodes.AuthErrorCode;
import com.example.nsu_backend.errorCodes.DatabaseErrorCode;
import com.example.nsu_backend.errorCodes.GeneralErrorCode;
import com.example.nsu_backend.exceptions.InvalidCategoryException;
import com.example.nsu_backend.exceptions.TokenRefreshException;
import com.example.nsu_backend.exceptions.UserLoginException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Catches generic database issues (connection, syntax, etc.)
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDatabaseErrors(DataAccessException e) {
        ErrorDetail error = new ErrorDetail(DatabaseErrorCode.DB_ERROR.name(), e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(error));
    }

    // Catches specific constraint violations (Unique keys, Foreign keys)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataConflicts(DataIntegrityViolationException e) {
        ErrorDetail error = new ErrorDetail(DatabaseErrorCode.DATA_CONSTRAINT_VIOLATION.name(), e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(error));
    }

    // Handled authorization exceptions
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedExceptions(AuthorizationDeniedException e) {
        ErrorDetail error = new ErrorDetail(AuthErrorCode.NOT_ALLOWED.name(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(error));
    }

    // Handled invalid category exceptions
    @ExceptionHandler(InvalidCategoryException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedExceptions(InvalidCategoryException e) {
        ErrorDetail error = new ErrorDetail(GeneralErrorCode.INVALID_FIELD.name(), e.getMessage());
        return ResponseEntity.status(BAD_REQUEST)
                .body(new ApiResponse<>(error));
    }

    // Handle token refresh exceptions
    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenRefreshExceptions(TokenRefreshException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(new ErrorDetail(AuthErrorCode.EXPIRED_REFRESH_TOKEN.name(), e.getMessage())));
    }

    //Handle user login exceptions
    @ExceptionHandler(UserLoginException.class)
    public ResponseEntity<ApiResponse<Void>> handleLoginExceptions(UserLoginException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(new ErrorDetail(AuthErrorCode.INVALID_USER.name(), e.getMessage())));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidHttpRequestMethods(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ApiResponse<>(new ErrorDetail(ApiErrorCode.UNSUPPORTED_HTTP_METHOD.name(), e.getMessage())));
    }

    //Handles exceptions from @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ErrorDetail> errors = new ArrayList<>();

        // Extract each field name and its corresponding error message
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.add(new ErrorDetail(
                    GeneralErrorCode.INVALID_FIELD.name(),
                    error.getDefaultMessage(),
                    error.getField()));
        });

        return ResponseEntity.status(BAD_REQUEST)
                .body(new ApiResponse<>(errors));

    }
}