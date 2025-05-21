package com.example.demo.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus is a convenient way to map this exception directly to an HTTP status code
@ResponseStatus(HttpStatus.CONFLICT)

public class DuplicateContactInfoException extends RuntimeException  {
    public DuplicateContactInfoException(String message) {
        super(message);
    }

}
