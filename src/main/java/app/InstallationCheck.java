package app;
import java.io.InputStream;
import java.util.Properties;
/** Build-time native-image smoke check. Opens no UI and no database connection. */
public final class InstallationCheck {
    private InstallationCheck() { }
    static void requireJava21(int feature){if(feature!=21)throw new IllegalStateException("Queue-Clearance packages must use Java 21.");}
    public static void main(String[] args)throws Exception {
        requireJava21(Runtime.version().feature());
        verifyDependencies();
        System.out.println("Installation check passed: Java 21, runtime dependencies, Unicode charset, TLS cryptography and safe configuration template. No database connection opened.");
    }
    static void verifyDependencies()throws Exception {
        for(String name:new String[]{"com.formdev.flatlaf.FlatLightLaf","com.zaxxer.hikari.HikariDataSource","com.mysql.cj.jdbc.Driver",
                "org.mindrot.jbcrypt.BCrypt","org.openpdf.text.pdf.PdfWriter","org.apache.poi.xssf.usermodel.XSSFWorkbook",
                "ch.qos.logback.classic.LoggerContext","org.apache.logging.slf4j.SLF4JProvider"})Class.forName(name);
        if(java.security.Security.getProvider("SunEC")==null)throw new IllegalStateException("The packaged runtime is missing EC cryptography.");
        java.nio.charset.Charset.forName("windows-1252");
        Properties template=new Properties();try(InputStream stream=InstallationCheck.class.getResourceAsStream("/app.properties.example")){
            if(stream==null)throw new IllegalStateException("Configuration template is missing.");template.load(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8));
        }
        if(!"".equals(template.getProperty("db.password"))||!"false".equals(template.getProperty("backup.enabled")))throw new IllegalStateException("The packaged template must contain no database password and must disable backup.");
    }
}
