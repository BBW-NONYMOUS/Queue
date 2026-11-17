package config;
import java.nio.file.Path;
/** Paths are external configuration, resolved relative to app.properties. No backup secret is held here. */
public record OutputSettings(Path reports,Path pdfFont,boolean backupEnabled,Path dumpExecutable,Path backupOptions,
        Path backups,int backupTimeoutSeconds) { }
