/**
 * @ (#) GlobalExceptionHandler.java      2/16/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import vn.edu.iuh.fit.dtos.response.ApiResponse;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 2/16/2025
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> globalExceptionHandler(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ApiResponse.builder()
                                .status("FAILED")
                                .message(ex.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(MissingTokenException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingTokenException(MissingTokenException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ApiResponse.builder()
                                .status("FAILED")
                                .message(ex.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidTokenException(InvalidTokenException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ApiResponse.builder()
                                .status("FAILED")
                                .message(ex.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(TokenRevokedException.class)
    public ResponseEntity<ApiResponse<?>> handleTokenRevokedException(TokenRevokedException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ApiResponse.builder()
                                .status("FAILED")
                                .message(ex.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.builder()
                                .status("FAILED")
                                .message(ex.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorizedException(UnauthorizedException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ApiResponse.builder()
                                .status("FAILED")
                                .message(ex.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.builder()
                                .status("FAILED")
                                .message(ex.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponse.builder()
                                .status("FAILED")
                                .message(ex.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidPasswordException(InvalidPasswordException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ApiResponse.builder()
                                .status("FAILED")
                                .message(ex.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponse.builder()
                                .status("FAILED")
                                .message(ex.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleTokenNotFoundException(TokenNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponse.builder()
                                .status("FAILED")
                                .message(ex.getMessage())
                                .build()
                );
    }
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .status("FAILED")
                        .message("Validation failed")
                        .response(errors)
                        .build());
    }


    @ExceptionHandler(FriendRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleFriendRequestException(FriendRequestException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponse.builder()
                                .status("FAILED")
                                .message(ex.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(ConversationCreationException.class)
    public ResponseEntity<ApiResponse<?>> handleConversationCreationException(ConversationCreationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.builder()
                                .status("FAILED")
                                .message(ex.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(ConversationNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleConversationNotFoundException(ConversationNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponse.builder()
                                .status("FAILED")
                                .message(ex.getMessage())
                                .build()
                );
    }
}
