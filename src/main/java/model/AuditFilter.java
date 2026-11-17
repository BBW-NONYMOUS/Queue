package model;
import java.time.LocalDate;
public record AuditFilter(String table,String actor,String recordKey,LocalDate from,LocalDate through,int page) { }
