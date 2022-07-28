package com.wyc.model;

import com.wyc.enums.EnumErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    int errorCode;
    String message;
    boolean success;
    T data;

    public static <T> Result<T> error(String errorMessage) {
        return new Result<>(EnumErrorCode.COMMON_ERROR, errorMessage, false, null);
    }

    public static <T> Result<T> error(int errorCode, String errorMessage) {
        return new Result<>(errorCode, errorMessage, false, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(0, null, true, data);
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result<>(0, message, true, data);
    }
}
