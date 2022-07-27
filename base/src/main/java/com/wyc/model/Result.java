package com.wyc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    String message;
    Boolean success;
    T data;

    public static <T> Result<T> error(String errorMessage) {
        return new Result<>(errorMessage, false, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(null, true, data);
    }
}
