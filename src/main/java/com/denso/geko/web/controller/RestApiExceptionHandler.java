package com.denso.geko.web.controller;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class RestApiExceptionHandler extends ResponseEntityExceptionHandler {
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> hadleConstrainViolationException(
            ConstraintViolationException ex, WebRequest request) {
        return super.handleExceptionInternal(ex, ex.getMessage(), null, HttpStatus.BAD_REQUEST, request);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> hadleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        return super.handleExceptionInternal(ex, ex.getMessage(), null, HttpStatus.BAD_REQUEST, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(),headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(
            BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(),headers, status, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (body == null) {
            body = ex.getClass().toString();
        }
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }
}