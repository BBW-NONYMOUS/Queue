package dao.support;
import java.util.*;
/** Parses the checked-in mysql-client migration scripts, including DELIMITER blocks. */
public final class SqlScript {
    private SqlScript() { }
    public static List<String> statements(String source) {
        List<String> result=new ArrayList<>();String delimiter=";";StringBuilder pending=new StringBuilder();
        for(String line:source.replace("\uFEFF","").split("\\R")) {
            String trimmed=line.strip();
            if(trimmed.isEmpty()||trimmed.startsWith("--"))continue;
            if(trimmed.toUpperCase(Locale.ROOT).startsWith("DELIMITER ")) {
                if(!pending.toString().isBlank())throw new IllegalArgumentException("Delimiter inside an unfinished statement");
                delimiter=trimmed.substring(10).strip();if(!Set.of(";","$$").contains(delimiter))throw new IllegalArgumentException("Unexpected migration delimiter");continue;
            }
            pending.append(line).append('\n');
            String assembled=pending.toString().stripTrailing();
            if(assembled.endsWith(delimiter)){result.add(assembled.substring(0,assembled.length()-delimiter.length()).strip());pending.setLength(0);}
        }
        if(!pending.toString().isBlank())throw new IllegalArgumentException("Unterminated migration statement");
        return List.copyOf(result);
    }
}
