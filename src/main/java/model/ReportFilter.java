package model;
import java.time.LocalDate;
public record ReportFilter(ReportType type,Long termId,Long orgId,String studentNumber,String program,Integer yearLevel,
        String section,LocalDate from,LocalDate through,boolean includeInactive) { }
