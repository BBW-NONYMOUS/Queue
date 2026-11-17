package model;
public record BackupResult(java.nio.file.Path directory,long bytes,String sha256,java.time.LocalDateTime serverStartedAt) { }
