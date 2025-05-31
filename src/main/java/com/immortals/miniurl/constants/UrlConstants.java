package com.immortals.miniurl.constants;

public class UrlConstants {

    public static final int MAX_SHA256_BASE64URL_LENGTH = 43;
    public static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final int MAX_RANDOM_LENGTH = 10;

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    public static final String MDC_USER_AGENT_KEY = "userAgent";

    public static final Long MAX_AGE_CORS_SECS = 86400L;
    public static final Long MAX_AGE_HSTS_SECS = 86400L;

    public static final int RANDOM_LENGTH = 7;
    public static final int HASH_LENGTH = 10;
    public static final int TIMESTAMP_RANDOM_LENGTH = 6;
    public static final String HASH_ALGORITHM = "SHA-256";


    public static final int MACHINE_ID = 1;
    public static final long EPOCH = 1609459200000L; // Jan 1, 2021
    public static final int MACHINE_ID_BITS = 5;
    public static final int SEQUENCE_BITS = 12;

    public static final String[] RESERVED_ALIASES = {"admin", "login", "signup", "dashboard", "api", "support", "help", "contact"};

    public static final int MAX_TOKENS = 100;     // max tokens in bucket
    public static final int REFILL_TOKENS_PER_SECONDS = 10;
}
