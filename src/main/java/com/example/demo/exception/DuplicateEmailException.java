// src/main/java/com/example/demo/exception/DuplicateEmailException.java
package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus is a convenient way to map this exception directly to an HTTP status code
@ResponseStatus(HttpStatus.CONFLICT) // HTTP 409 Conflict
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}