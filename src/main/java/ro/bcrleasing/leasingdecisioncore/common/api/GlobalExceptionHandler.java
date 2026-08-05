package ro.bcrleasing.leasingdecisioncore.common.api;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ro.bcrleasing.leasingdecisioncore.common.exception.ExternalCapabilityException;
import ro.bcrleasing.leasingdecisioncore.common.exception.InvalidSubjectDataException;
import ro.bcrleasing.leasingdecisioncore.common.exception.ResourceNotFoundException;
import ro.bcrleasing.leasingdecisioncore.common.exception.UnsupportedSubjectTypeException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        return response(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                exception.getMessage(),
                List.of()
        );
    }

    @ExceptionHandler({
            InvalidSubjectDataException.class,
            UnsupportedSubjectTypeException.class
    })
    public ResponseEntity<ApiErrorResponse> handleInvalidSubject(RuntimeException exception) {
        return response(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "INVALID_SUBJECT_DATA",
                exception.getMessage(),
                List.of()
        );
    }

    @ExceptionHandler(ExternalCapabilityException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalCapability(
            ExternalCapabilityException exception
    ) {
        LOGGER.warn(
                "Capability call failed. capability={}, errorType={}",
                exception.getCapability(),
                exception.getClass().getSimpleName()
        );

        return response(
                HttpStatus.SERVICE_UNAVAILABLE,
                "CAPABILITY_UNAVAILABLE",
                "Blacklist assessment could not be completed because a required capability is unavailable.",
                List.of(exception.getCapability())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        List<String> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .toList();

        return response(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "The request is invalid.",
                details
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException exception
    ) {
        return response(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST_BODY",
                "The request body is missing or cannot be parsed.",
                List.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        LOGGER.error(
                "Unexpected error while processing a decision request. errorType={}",
                exception.getClass().getName(),
                exception
        );

        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "An unexpected error occurred.",
                List.of()
        );
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private ResponseEntity<ApiErrorResponse> response(
            HttpStatus status,
            String code,
            String message,
            List<String> details
    ) {
        return ResponseEntity.status(status).body(
                new ApiErrorResponse(
                        Instant.now(),
                        status.value(),
                        code,
                        message,
                        details
                )
        );
    }
}
