package model;
public record BoardRow(long orgId, OfficeCode office, String name, boolean active, boolean open,
        String number, String status, long waiting) { }
