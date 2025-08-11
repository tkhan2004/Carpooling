package org.example.carpooling.Payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

public class ApiResponse<T> {
    private boolean isSuccess;
    private String message;

    public ApiResponse(boolean isSuccess, String message, T data) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }



    private T data;
}
