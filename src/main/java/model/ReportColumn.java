package model;
public record ReportColumn(String label,ReportValueType type) {
    public ReportColumn(String label,boolean money){this(label,money?ReportValueType.MONEY:ReportValueType.TEXT);}
    public boolean money(){return type==ReportValueType.MONEY;}
}
