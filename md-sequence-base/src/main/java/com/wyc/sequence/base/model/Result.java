package com.wyc.sequence.base.model;

import com.wyc.sequence.base.enums.EnumErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    int errCode;
    String msg;
    boolean success;
    T data;

    public static <T> Result<T> error(String errorMessage) {
        return new Result<>(EnumErrorCode.CommonError, errorMessage, false, null);
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
