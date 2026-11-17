package app;
import java.nio.file.*;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class PackagedLauncherTest {
    @TempDir Path temporary;
    @Test void usesPerUserWritableFolder(){assertEquals(temporary.resolve("Queue-Clearance"),PackagedLauncher.dataDirectory(Map.of("LOCALAPPDATA",temporary.toString()),null));}
    @Test void fallsBackToUserHome(){assertEquals(temporary.resolve(".queue-clearance"),PackagedLauncher.dataDirectory(Map.of(),temporary.toString()));}
    @Test void rejectsRelativeOrMissingPaths(){assertThrows(IllegalArgumentException.class,()->PackagedLauncher.dataDirectory(Map.of("LOCALAPPDATA","relative"),temporary.toString()));assertThrows(IllegalArgumentException.class,()->PackagedLauncher.dataDirectory(Map.of(),null));}
    @Test void createsOnlyNonSecretTemplateAndPreservesEdits()throws Exception{
        Path file=temporary.resolve("data/app.properties");PackagedLauncher.prepareConfiguration(file);String text=Files.readString(file);
        assertTrue(text.contains("backup.enabled=false"));assertTrue(text.lines().anyMatch(line->line.equals("db.password=")));
        Files.writeString(file,"operator configuration");PackagedLauncher.prepareConfiguration(file);assertEquals("operator configuration",Files.readString(file));
    }
    @Test void refusesDirectoryAsConfiguration()throws Exception{assertThrows(java.io.IOException.class,()->PackagedLauncher.prepareConfiguration(temporary));}
}
