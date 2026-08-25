package ru.azorium.launchserver.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.azorium.launchserver.exception.ClientFolderAlreadyExistException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientFolderAlreadyExistException.class)
    public ResponseEntity<String> handleResponseStatus(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

}
