package org.example.carpooling.Exception;

public class Exception extends RuntimeException {
    public Exception(String email) {
        super("Email '" + email + "' đã được sử dụng");
    }
}
