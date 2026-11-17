package architecture;
import java.nio.file.*;import java.util.*;import java.util.regex.*;import javax.xml.parsers.DocumentBuilderFactory;import org.w3c.dom.*;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class FormContractTest {
    @Test void netbeansFormsMatchGuardedSourcesAndCompleteLayouts()throws Exception {
        var parser=DocumentBuilderFactory.newInstance();parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        try(var paths=Files.walk(Path.of("src/main/java/ui"))){for(Path form:paths.filter(p->p.toString().endsWith(".form")).toList()){
            String java=Files.readString(form.resolveSibling(form.getFileName().toString().replace(".form",".java")));assertTrue(java.contains("//GEN-BEGIN:initComponents"),form.toString());assertTrue(java.contains("//GEN-BEGIN:variables"),form.toString());
            Document xml=parser.newDocumentBuilder().parse(form.toFile());NodeList handlers=xml.getElementsByTagName("EventHandler");
            for(int i=0;i<handlers.getLength();i++){String handler=((Element)handlers.item(i)).getAttribute("handler");assertTrue(Pattern.compile("void\\s+"+handler+"\\s*\\(").matcher(java).find(),form+" / "+handler);}
            NodeList layouts=xml.getElementsByTagName("Layout");for(int i=0;i<layouts.getLength();i++){
                Element layout=(Element)layouts.item(i);if(layout.hasAttribute("class"))continue;Element container=(Element)layout.getParentNode();Set<String> children=new HashSet<>();
                for(Node child=container.getFirstChild();child!=null;child=child.getNextSibling())if(child instanceof Element e&&e.getTagName().equals("SubComponents"))for(Node component=e.getFirstChild();component!=null;component=component.getNextSibling())if(component instanceof Element c)children.add(c.getAttribute("name"));
                NodeList dimensions=layout.getElementsByTagName("DimensionLayout");assertEquals(2,dimensions.getLength(),form.toString());for(int d=0;d<dimensions.getLength();d++){NodeList references=((Element)dimensions.item(d)).getElementsByTagName("Component");Set<String> actual=new HashSet<>();for(int c=0;c<references.getLength();c++)assertTrue(actual.add(((Element)references.item(c)).getAttribute("id")),"Duplicate layout child");assertEquals(children,actual,form.toString());}
            }
        }}
    }
    @Test void jdbcDoesNotLeakIntoFormsOrServices()throws Exception {
        try(var paths=Files.walk(Path.of("src/main/java"))){for(Path file:paths.filter(p->p.toString().endsWith(".java")).toList()){String source=Files.readString(file);if(source.contains("java.sql."))assertTrue(file.startsWith(Path.of("src/main/java/dao")),file.toString());assertFalse(source.contains(".createStatement("),file.toString());}}
    }
}
