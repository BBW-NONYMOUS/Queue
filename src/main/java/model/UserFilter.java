package model;

public record UserFilter(String query, Role role, Boolean active, int page, int pageSize) {
}
