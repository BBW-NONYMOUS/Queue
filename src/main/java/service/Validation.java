package service;

import java.time.LocalDateTime;
import java.util.Objects;

final class Validation {
    private Validation() {
    }

    static String required(String value, String label, int maximum) throws ValidationException {
        String result = optional(value, label, maximum);
        if (result == null) { throw new ValidationException("Enter " + label.toLowerCase(java.util.Locale.ROOT) + "."); }
        return result;
    }

    static String optional(String value, String label, int maximum) throws ValidationException {
        if (value == null || value.isBlank()) { return null; }
        String result = value.strip();
        if (result.codePointCount(0, result.length()) > maximum || result.codePoints().anyMatch(Character::isISOControl)) {
            throw new ValidationException(label + " must be at most " + maximum + " characters without control characters.");
        }
        return result;
    }

    static String filter(String value, String label, int maximum) throws ValidationException {
        String result = optional(value, label, maximum);
        return result == null ? "" : result;
    }

    static void page(int page, int size) throws ValidationException {
        if (page < 0 || page > 100000 || size < 1 || size > 100) {
            throw new ValidationException("Choose a valid results page and a page size from 1 to 100.");
        }
    }

    static void unchanged(LocalDateTime expected, LocalDateTime current) throws RecordChangedException {
        if (expected == null || !Objects.equals(expected, current)) { throw new RecordChangedException(); }
    }
}
