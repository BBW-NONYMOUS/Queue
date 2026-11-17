package config;
import java.nio.file.*;import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.*;import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class AppConfigTest {
    @TempDir Path temporary;
    private AppConfig load(String replacements)throws Exception{
        String template=Files.readString(Path.of("app.properties.example"),StandardCharsets.UTF_8).replace("db.password=","db.password=test-only-not-a-campus-secret");
        Files.writeString(temporary.resolve("app.properties"),template+"\n"+replacements,StandardCharsets.UTF_8);return AppConfig.load(temporary.resolve("app.properties"));
    }
    @Test void keepsCredentialsExternalAndResolvesOutputsBesideConfig()throws Exception{AppConfig config=load("");assertEquals(temporary.resolve("reports"),config.outputs().reports());assertFalse(config.outputs().backupEnabled());assertFalse(config.toString().contains("test-only"));}
    @Test void rejectsInsecureTls()throws Exception{assertThrows(ConfigurationException.class,()->load("db.url=jdbc:mysql://localhost:3306/queue_clearance?sslMode=DISABLED"));}
    @Test void rejectsInvalidPoolRelationship(){assertThrows(ConfigurationException.class,()->load("db.pool.minimumIdle=6\ndb.pool.maximumPoolSize=5"));}
    @Test void requiresThreeSecondPollInterval(){assertThrows(ConfigurationException.class,()->load("app.poll.interval.ms=1000"));}
    @Test void rejectsUnsupportedBackupFlag(){assertThrows(ConfigurationException.class,()->load("backup.enabled=yes"));}
}
