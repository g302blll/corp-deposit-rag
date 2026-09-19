package com.ljp.corpdeposit.web;

import java.time.Instant;

public record ApiError(String code, String message, Instant timestamp) {
}

