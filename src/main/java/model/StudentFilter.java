package model;

public record StudentFilter(String query, String program, Integer yearLevel,
        String section, Boolean active, int page, int pageSize) {
}
