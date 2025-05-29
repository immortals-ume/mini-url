package com.immortals.miniurl.utils;

import static com.immortals.miniurl.constants.UrlConstants.ALPHABET;

public class Base62Utils {
    public static String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be non-negative");
        }
        if (value == 0) return "0";

        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            int remainder = (int) (value % 62);
            sb.append(ALPHABET.charAt(remainder));
            value /= 62;
        }
        return sb.reverse()
                .toString();
    }

    // Optional: Decode method
    public static long decode(String base62) {
        long result = 0;
        for (char c : base62.toCharArray()) {
            int index = ALPHABET.indexOf(c);
            if (index == -1) throw new IllegalArgumentException("Invalid character in base62 string");
            result = result * 62 + index;
        }
        return result;
    }
}
