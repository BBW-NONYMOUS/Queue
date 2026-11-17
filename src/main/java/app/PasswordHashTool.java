package app;

import java.io.Console;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.mindrot.jbcrypt.BCrypt;

/** Offline installation helper; prints a BCrypt hash, never the password. */
public final class PasswordHashTool {
    private static final int BCRYPT_COST = 12;

    private PasswordHashTool() {
    }

    public static void main(String[] args) {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Run from a terminal with java, not an IDE output window.");
        }
        char[] password = console.readPassword("New administrator password: ");
        char[] confirmation = console.readPassword("Confirm password: ");
        try {
            if (password == null || confirmation == null || !Arrays.equals(password, confirmation)) {
                throw new IllegalArgumentException("Passwords did not match.");
            }
            String value = new String(password);
            int byteLength = value.getBytes(StandardCharsets.UTF_8).length;
            if (value.isBlank() || password.length < 12 || byteLength > 72) {
                throw new IllegalArgumentException("Use at least 12 characters and at most 72 UTF-8 bytes.");
            }
            console.printf("%s%n", BCrypt.hashpw(value, BCrypt.gensalt(BCRYPT_COST)));
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
            if (confirmation != null) {
                Arrays.fill(confirmation, '\0');
            }
        }
    }
}
