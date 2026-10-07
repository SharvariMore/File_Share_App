package in.sharvarimore.fileshareapi.exceptions;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice      // handles exceptions in REST APIs

public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)     // handle exceptions at controller level
    public ResponseEntity<?> handleDuplicateEmailException(DuplicateKeyException ex) {
        Map<String, Object> data = new HashMap<>();
        data.put("status", HttpStatus.CONFLICT);
        data.put("message", ex.getMessage());          // return exception message
        return ResponseEntity.status(HttpStatus.CONFLICT).body(data);
    }
}
