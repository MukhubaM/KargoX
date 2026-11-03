package com.kargox.util;

import java.security.SecureRandom;
import java.util.UUID;

public final class CodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private CodeGenerator() {
    }

    public static String bookingReference() {

        return "KGX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // 4-digit numeric PIN used for proof-of-pickup & proof-of-delivery
    public static String numericPin() {

        return String.format("%04d", RANDOM.nextInt(10000));
    }
}
