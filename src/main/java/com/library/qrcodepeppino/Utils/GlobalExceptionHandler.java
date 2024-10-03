package com.library.qrcodepeppino.Utils;

import com.library.qrcodepeppino.Exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<Class<? extends RuntimeException>, HttpStatus> exceptionMap = new HashMap<>();

    static {
        exceptionMap.put(RuntimeException.class, HttpStatus.BAD_REQUEST);
        exceptionMap.put(ColorNotValidException.class, HttpStatus.BAD_REQUEST);
        exceptionMap.put(BorderColorNotValidException.class, HttpStatus.BAD_REQUEST);
        exceptionMap.put(TemplateNotFoundException.class, HttpStatus.BAD_REQUEST);
        exceptionMap.put(ImageNullException.class, HttpStatus.INTERNAL_SERVER_ERROR);
        exceptionMap.put(ImageUrlNotFoundException.class, HttpStatus.INTERNAL_SERVER_ERROR);
        exceptionMap.put(ImageUrlNotValidException.class, HttpStatus.INTERNAL_SERVER_ERROR);
        exceptionMap.put(LogoNotFoundException.class, HttpStatus.INTERNAL_SERVER_ERROR);
        exceptionMap.put(InvalidDimensionLogoException.class, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException e) {
        e.printStackTrace();

        HttpStatus status = exceptionMap.getOrDefault(e.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
        String errorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", errorMessage);

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception e) {
        e.printStackTrace();
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "C'è stato un errore durante la creazione del qrCode: " + e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
