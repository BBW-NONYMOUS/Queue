package dao.support;
import java.io.*;import java.net.URI;import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.util.*;
/** No production app.properties fallback. Loading this never opens a connection. */
public record MySqlSettings(String rootUrl,String owner,String ownerPassword,String client,String clientPassword,String clientHost,String serverUuid,Path project) {
    public static final String SCHEMA="queue_clearance_test";
    public static MySqlSettings load(Path path)throws IOException {
        Properties p=new Properties();try(Reader reader=Files.newBufferedReader(path,StandardCharsets.UTF_8)){p.load(reader);}
        if(!"RECREATE_queue_clearance_test_ON_ISOLATED_SERVER".equals(p.getProperty("mysql.it.confirm")))throw new IllegalArgumentException("Set the explicit disposable-schema confirmation in the integration configuration.");
        String url=required(p,"mysql.it.url");if(!url.startsWith("jdbc:mysql://"))throw new IllegalArgumentException("Use a root MySQL JDBC URL for the isolated server.");
        URI parsed;try{parsed=URI.create(url.substring("jdbc:".length()));}catch(IllegalArgumentException ex){throw new IllegalArgumentException("The isolated server JDBC URL is invalid.");}
        if(!url.startsWith("jdbc:mysql://")||!"mysql".equals(parsed.getScheme())||parsed.getHost()==null||!Set.of("","/").contains(parsed.getPath())||parsed.getUserInfo()!=null||parsed.getFragment()!=null)throw new IllegalArgumentException("mysql.it.url must address a single test server without a database or credentials.");
        String query=parsed.getRawQuery();if(query==null||!Arrays.asList(query.split("&")).contains("sslMode=VERIFY_IDENTITY")||query.toLowerCase(Locale.ROOT).matches(".*(?:^|&)(?:user|password|allowmultiqueries)=.*"))throw new IllegalArgumentException("Use verified TLS and separate credentials for integration tests.");
        String client=required(p,"mysql.it.client.username"),host=required(p,"mysql.it.client.grantHost"),uuid=required(p,"mysql.it.expectedServerUuid");
        if(!client.equals("queue_it_app")||!host.matches("[A-Za-z0-9_.:-]{1,255}")||!uuid.matches("[a-fA-F0-9-]{36}"))throw new IllegalArgumentException("Use queue_it_app, an exact grant host, and the isolated server UUID.");
        return new MySqlSettings(url,required(p,"mysql.it.owner.username"),required(p,"mysql.it.owner.password"),client,required(p,"mysql.it.client.password"),host,uuid,Path.of("").toAbsolutePath().normalize());
    }
    private static String required(Properties p,String key){String value=p.getProperty(key);if(value==null||value.isBlank())throw new IllegalArgumentException("Set "+key+" in the integration configuration.");return value;}
    public String databaseUrl(){int at=rootUrl.indexOf('?');return rootUrl.substring(0,at)+(rootUrl.substring(0,at).endsWith("/")?"":"/")+SCHEMA+rootUrl.substring(at);}
    @Override public String toString(){return "MySqlSettings[schema="+SCHEMA+", credentials=REDACTED]";}
}
