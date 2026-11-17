package model;
import java.time.LocalDateTime;
import java.util.*;
public record ReportData(String title,LocalDateTime generatedAt,String scope,List<ReportColumn> columns,List<List<Object>> rows) {
    public ReportData {columns=List.copyOf(columns);rows=rows.stream().map(row->Collections.unmodifiableList(new ArrayList<Object>(row))).toList();}
}
