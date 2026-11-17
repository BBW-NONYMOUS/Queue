package app;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class InstallationCheckTest {
    @Test void pinnedRuntimeLibrariesAndSafeTemplateAreAvailable()throws Exception{InstallationCheck.verifyDependencies();}
    @Test void refusesWrongRuntimeVersion(){assertDoesNotThrow(()->InstallationCheck.requireJava21(21));assertThrows(IllegalStateException.class,()->InstallationCheck.requireJava21(26));assertThrows(IllegalStateException.class,()->InstallationCheck.requireJava21(17));}
}
