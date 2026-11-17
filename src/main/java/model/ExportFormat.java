package model;
public enum ExportFormat { PDF("pdf"),EXCEL("xlsx");private final String extension;ExportFormat(String extension){this.extension=extension;}public String extension(){return extension;} }
