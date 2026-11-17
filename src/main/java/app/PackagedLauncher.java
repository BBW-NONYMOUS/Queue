package app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Native entry point. Bootstrap writable per-user paths before Logback initializes. */
public final class PackagedLauncher {
    private PackagedLauncher() { }

    public static void main(String[] args) {
        try {
            Path directory = dataDirectory(System.getenv(), System.getProperty("user.home"));
            Files.createDirectories(directory.resolve("logs"));
            if (System.getProperty("queueclearance.config") == null) {
                Path configuration = directory.resolve("app.properties");
                prepareConfiguration(configuration);
                System.setProperty("queueclearance.config", configuration.toString());
            }
            if (System.getProperty("queueclearance.logDir") == null) {
                System.setProperty("queueclearance.logDir", directory.resolve("logs").toString());
            }
            Application.main(args);
        } catch (IOException | IllegalArgumentException ex) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Cannot prepare Queue-Clearance's configuration folder. Check your application-data folder permissions.",
                    "Queue-Clearance | Cannot start", JOptionPane.ERROR_MESSAGE));
        }
    }

    static Path dataDirectory(Map<String, String> environment, String home) {
        String local = environment.get("LOCALAPPDATA");
        if (local != null && !local.isBlank()) {
            Path path = Path.of(local);
            if (!path.isAbsolute()) { throw new IllegalArgumentException("LOCALAPPDATA must be absolute"); }
            return path.resolve("Queue-Clearance").normalize();
        }
        if (home == null || home.isBlank() || !Path.of(home).isAbsolute()) {
            throw new IllegalArgumentException("An absolute user home is required");
        }
        return Path.of(home).resolve(".queue-clearance").normalize();
    }

    static void prepareConfiguration(Path destination) throws IOException {
        if (Files.exists(destination)) {
            if (!Files.isRegularFile(destination)) { throw new IOException("Configuration is not a regular file"); }
            return;
        }
        Files.createDirectories(destination.getParent());
        try (InputStream template = PackagedLauncher.class.getResourceAsStream("/app.properties.example")) {
            if (template == null) { throw new IOException("Configuration template is missing from the application"); }
            try { Files.copy(template, destination); }
            catch (FileAlreadyExistsException race) {
                if (!Files.isRegularFile(destination)) { throw race; }
            }
        }
    }
}
