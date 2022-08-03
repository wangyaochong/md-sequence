package com.wyc.enums;

public class EnumErrorCode {
    public static final int NoError = 0;
    public static final int CommonError = -1;
    public static final int NoAvailableNode = -2;
    public static final int NotUsingTargetNodeException = -3;
    public static final int SeqNameNotFoundInDbException = -4;

    public static final int TooMuchRetry = -5;
}
