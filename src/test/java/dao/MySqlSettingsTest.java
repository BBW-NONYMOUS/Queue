package dao;
import dao.support.MySqlSettings;import java.nio.file.*;import org.junit.jupiter.api.*;import org.junit.jupiter.api.io.TempDir;import static org.junit.jupiter.api.Assertions.*;
class MySqlSettingsTest {
    @TempDir Path temporary;
    private Path config(String extra)throws Exception{String source=Files.readString(Path.of("database/test/mysql-it.properties.example")).replace("mysql.it.expectedServerUuid=","mysql.it.expectedServerUuid=12345678-1234-1234-1234-123456789abc").replace("mysql.it.owner.password=","mysql.it.owner.password=test-only-owner").replace("mysql.it.client.password=","mysql.it.client.password=test-only-client");Path file=temporary.resolve("it.properties");Files.writeString(file,source+"\n"+extra);return file;}
    @Test void neverTargetsProductionSchema()throws Exception{var config=MySqlSettings.load(config(""));assertTrue(config.databaseUrl().contains("/queue_clearance_test?"));assertFalse(config.toString().contains("test-only"));}
    @Test void requiresExplicitAcknowledgement()throws Exception{Path file=config("mysql.it.confirm=no");assertThrows(IllegalArgumentException.class,()->MySqlSettings.load(file));}
    @Test void rejectsDatabaseInOwnerUrl()throws Exception{Path file=config("mysql.it.url=jdbc:mysql://localhost:3306/queue_clearance?sslMode=VERIFY_IDENTITY");assertThrows(IllegalArgumentException.class,()->MySqlSettings.load(file));}
    @Test void rejectsUnverifiedTls()throws Exception{Path file=config("mysql.it.url=jdbc:mysql://localhost:3306/?sslMode=DISABLED");assertThrows(IllegalArgumentException.class,()->MySqlSettings.load(file));}
}
