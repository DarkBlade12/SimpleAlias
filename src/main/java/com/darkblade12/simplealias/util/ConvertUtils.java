package com.darkblade12.simplealias.util;

import java.util.regex.Pattern;

public final class ConvertUtils {
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMPTY_PATTERN = Pattern.compile("<(empty|null)>|\\s+", Pattern.CASE_INSENSITIVE);
    public static final Pattern SEPARATOR_PATTERN = Pattern.compile("\\s?,\\s?");

    private ConvertUtils() {
    }

    public static boolean isEmpty(String value) {
        return value.isEmpty() || EMPTY_PATTERN.matcher(value).matches();
    }

    public static boolean convertToBoolean(String value) {
        if (!BOOLEAN_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid boolean value.");
        }

        return Boolean.parseBoolean(value);
    }

    public static String[] split(String value) {
        return SEPARATOR_PATTERN.split(value);
    }
}
