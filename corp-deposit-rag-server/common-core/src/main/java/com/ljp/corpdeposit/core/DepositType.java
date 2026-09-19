package com.ljp.corpdeposit.core;

public enum DepositType {
    DEMAND(1),
    TIME(2),
    NOTICE(3),
    LARGE_CERTIFICATE(4);

    private final int code;

    DepositType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}

