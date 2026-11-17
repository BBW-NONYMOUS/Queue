package config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Properties;

/** External UTF-8 configuration. Its string representation never exposes secrets. */
public final class AppConfig {
    private final Properties properties;
    private final Path source;

    private AppConfig(Path source, Properties properties) throws ConfigurationException {
        this.source = source;
        this.properties = properties;
        validate();
    }

    public static AppConfig load() throws ConfigurationException {
        try {
            return load(Path.of(System.getProperty("queueclearance.config", "app.properties")));
        } catch (IllegalArgumentException ex) {
            throw new ConfigurationException("The application configuration path is invalid.", ex);
        }
    }

    public static AppConfig load(Path path) throws ConfigurationException {
        Properties properties = new Properties();
        Path source = path.toAbsolutePath().normalize();
        try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException | IllegalArgumentException ex) {
            throw new ConfigurationException("Cannot read app.properties. Contact the administrator.", ex);
        }
        return new AppConfig(source, properties);
    }

    private void validate() throws ConfigurationException {
        String url = required("db.url");
        String normalized = url.toLowerCase(java.util.Locale.ROOT);
        if (!url.startsWith("jdbc:mysql://") || !normalized.contains("sslmode=verify_identity")
                || normalized.contains("allowmultiqueries=true")
                || normalized.contains("password=") || normalized.contains("user=")) {
            throw new ConfigurationException("Use a MySQL JDBC URL with sslMode=VERIFY_IDENTITY and separate credentials.");
        }
        required("db.username");
        required("db.password");
        number("db.pool.maximumPoolSize", 5, 1, 20);
        int minimum = number("db.pool.minimumIdle", 1, 0, 20);
        if (minimum > maximumPoolSize()) {
            throw new ConfigurationException("The minimum pool size cannot exceed the maximum pool size.");
        }
        number("db.pool.connectionTimeoutMs", 5000, 1000, 60000);
        int validation = number("db.pool.validationTimeoutMs", 3000, 250, 60000);
        if (validation >= connectionTimeoutMs()) {
            throw new ConfigurationException("The validation timeout must be shorter than the connection timeout.");
        }
        number("db.pool.idleTimeoutMs", 600000, 10000, 3600000);
        number("db.pool.maxLifetimeMs", 1500000, 30000, 3600000);
        number("auth.bcrypt.cost", 12, 12, 16);
        number("ui.font.size", 14, 12, 32);
        number("app.poll.interval.ms", 3000, 3000, 3000);
        number("backup.timeout.seconds",600,30,7200);
        if(!java.util.Set.of("true","false").contains(text("backup.enabled","false")))throw new ConfigurationException("Set backup.enabled to true or false.");
        if (!"TRANSACTION_READ_COMMITTED".equals(text("db.transaction.isolation", "TRANSACTION_READ_COMMITTED"))
                || !"+08:00".equals(text("db.session.timeZone", "+08:00"))) {
            throw new ConfigurationException("Use READ COMMITTED transactions and database session time zone +08:00.");
        }
        try { outputs(); } catch(java.nio.file.InvalidPathException ex) { throw new ConfigurationException("Check the configured report and backup paths.",ex); }
        try {
            ZoneId.of(text("app.timezone", "Asia/Manila"));
        } catch (DateTimeException ex) {
            throw new ConfigurationException("The application time zone is invalid.", ex);
        }
    }

    private String required(String key) throws ConfigurationException {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException("Set " + key + " in the external app.properties file.");
        }
        return value;
    }

    private int number(String key, int fallback, int minimum, int maximum) throws ConfigurationException {
        try {
            int value = Integer.parseInt(text(key, Integer.toString(fallback)));
            if (value < minimum || value > maximum) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new ConfigurationException("Check the numeric configuration value for " + key + ".");
        }
    }

    private int validatedNumber(String key, int fallback) {
        return Integer.parseInt(text(key, Integer.toString(fallback)));
    }

    private String text(String key, String fallback) {
        return properties.getProperty(key, fallback).trim();
    }

    public Path source() { return source; }
    public String applicationName() { return text("app.name", "Queue-Clearance"); }
    public String fontFamily() { return text("ui.font.family", "Segoe UI"); }
    public int fontSize() { return validatedNumber("ui.font.size", 14); }
    public String jdbcUrl() { return properties.getProperty("db.url").trim(); }
    public String databaseUsername() { return properties.getProperty("db.username").trim(); }
    public String databasePassword() { return properties.getProperty("db.password"); }
    public int maximumPoolSize() { return validatedNumber("db.pool.maximumPoolSize", 5); }
    public int minimumIdle() { return validatedNumber("db.pool.minimumIdle", 1); }
    public int connectionTimeoutMs() { return validatedNumber("db.pool.connectionTimeoutMs", 5000); }
    public int validationTimeoutMs() { return validatedNumber("db.pool.validationTimeoutMs", 3000); }
    public int idleTimeoutMs() { return validatedNumber("db.pool.idleTimeoutMs", 600000); }
    public int maxLifetimeMs() { return validatedNumber("db.pool.maxLifetimeMs", 1500000); }
    public int bcryptCost() { return validatedNumber("auth.bcrypt.cost", 12); }
    public int pollIntervalMs() { return validatedNumber("app.poll.interval.ms", 3000); }

    private Path outputPath(String key,String fallback) {
        Path path=Path.of(text(key,fallback));return path.isAbsolute()?path.normalize():source.getParent().resolve(path).normalize();
    }
    public OutputSettings outputs() {
        return new OutputSettings(outputPath("report.output.directory","reports"),outputPath("report.font.file","C:/Windows/Fonts/segoeui.ttf"),
                Boolean.parseBoolean(text("backup.enabled","false")),outputPath("backup.mysqldump.path","tools/mysqldump.exe"),
                outputPath("backup.options.file","backup-client.cnf"),outputPath("backup.output.directory","backups"),validatedNumber("backup.timeout.seconds",600));
    }

    @Override
    public String toString() {
        return "AppConfig[source=" + source + ", credentials=REDACTED]";
    }
}
