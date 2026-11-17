package model;
public enum ReportType {
    STUDENTS("Students"),QUEUE("Queue"),FINES("Fines"),PAYMENTS("Payments"),CLEARANCE("Clearance");
    private final String label;ReportType(String label){this.label=label;}@Override public String toString(){return label;}
}
