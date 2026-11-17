package model;

public record FineFilter(String studentNumber, Long orgId, Long termId, FineStatus status, int page, int pageSize) {
}
