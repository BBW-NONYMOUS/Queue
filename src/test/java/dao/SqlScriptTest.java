package dao;
import dao.support.SqlScript;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SqlScriptTest {
    @Test void retainsSemicolonsInsideTriggerBodies(){var sql=SqlScript.statements("DELIMITER $$\nCREATE TRIGGER x BEFORE INSERT ON t FOR EACH ROW BEGIN SET NEW.id=1; SET NEW.a=2; END$$\nDELIMITER ;\nSELECT 1;\n");assertEquals(2,sql.size());assertTrue(sql.getFirst().endsWith("END"));}
    @Test void allProductionMigrationsParse()throws Exception{try(var paths=Files.list(Path.of("database/migrations"))){for(Path p:paths.filter(p->p.toString().endsWith(".sql")).toList())assertFalse(SqlScript.statements(Files.readString(p)).isEmpty(),p.toString());}}
    @Test void rejectsPartialScripts(){assertThrows(IllegalArgumentException.class,()->SqlScript.statements("SELECT 1"));}
}
