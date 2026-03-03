package com.moneycontas.exception;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String VIOLATIONS_KEY = "violations";

    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed");
        problem.setType(URI.create("https://problems.moneycontas.com/validation-error"));
        problem.setTitle("Validation Error");
        problem.setProperty(TIMESTAMP_KEY, Instant.now());

        Map<String, String> violations = ex.getBindingResult().getAllErrors().stream()
                .filter(e -> e instanceof FieldError)
                .map(e -> (FieldError) e)
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid value",
                        (existing, replacement) -> existing));

        problem.setProperty(VIOLATIONS_KEY, violations);
        return problem;
    }

    
    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("https://problems.moneycontas.com/not-found"));
        problem.setTitle("Resource Not Found");
        problem.setProperty(TIMESTAMP_KEY, Instant.now());
        return problem;
    }

    
    @ExceptionHandler(EntityExistsException.class)
    public ProblemDetail handleEntityExists(EntityExistsException ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create("https://problems.moneycontas.com/conflict"));
        problem.setTitle("Resource Already Exists");
        problem.setProperty(TIMESTAMP_KEY, Instant.now());
        return problem;
    }

    
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource");
        problem.setType(URI.create("https://problems.moneycontas.com/forbidden"));
        problem.setTitle("Access Denied");
        problem.setProperty(TIMESTAMP_KEY, Instant.now());
        return problem;
    }

    
    @ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class })
    public ProblemDetail handleAuthentication(RuntimeException ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed: " + ex.getMessage());
        problem.setType(URI.create("https://problems.moneycontas.com/unauthorized"));
        problem.setTitle("Unauthorized");
        problem.setProperty(TIMESTAMP_KEY, Instant.now());
        return problem;
    }

    
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
        problem.setType(URI.create("https://problems.moneycontas.com/internal-error"));
        problem.setTitle("Internal Server Error");
        problem.setProperty(TIMESTAMP_KEY, Instant.now());
        return problem;
    }
}
