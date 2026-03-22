package com.codereview.util;

import org.springframework.util.StringUtils;
import org.owasp.encoder.Encode;

/**
 * StringUtil â€” validation, sanitisation, and common string helpers.
 * Skills: Secure Coding, Server Side
 */
public final class StringUtil {

    private StringUtil() {
    }

    /** True if the string is not null and not blank. */
    public static boolean hasText(String s) {
        return StringUtils.hasText(s);
    }

    /** Truncate to maxLength, appending "â€¦" if shortened. */
    public static String truncate(String s, int maxLength) {
        if (s == null)
            return null;
        return s.length() <= maxLength ? s : s.substring(0, maxLength - 1) + "â€¦";
    }

    /** Sanitises user input to prevent XSS â€” encodes HTML entities. */
    public static String sanitizeHtml(String input) {
        if (input == null)
            return null;
        return Encode.forHtml(input);
    }

    /** Removes leading/trailing whitespace and collapses internal runs. */
    public static String normalise(String s) {
        if (s == null)
            return null;
        return s.strip().replaceAll("\\s+", " ");
    }

    /**
     * Slices code to extract the lines between startLine and endLine (1-indexed).
     */
    public static String extractLines(String code, int startLine, int endLine) {
        if (code == null)
            return "";
        String[] lines = code.split("\n");
        int from = Math.max(0, startLine - 1);
        int to = Math.min(lines.length, endLine);
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++) {
            sb.append(lines[i]);
            if (i < to - 1)
                sb.append('\n');
        }
        return sb.toString();
    }

    /** Basic email format check (lightweight, without regex). */
    public static boolean isValidEmail(String email) {
        if (!hasText(email))
            return false;
        int at = email.indexOf('@');
        return at > 0 && email.indexOf('.', at) > at + 1;
    }

    /** Masks all but the last 4 characters of a sensitive string. */
    public static String mask(String value) {
        if (value == null || value.length() <= 4)
            return "****";
        return "*".repeat(value.length() - 4) + value.substring(value.length() - 4);
    }
}
