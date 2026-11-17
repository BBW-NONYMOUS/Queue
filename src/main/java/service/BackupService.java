package service;
import config.OutputSettings;
import dao.UnitOfWork;
import model.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
/** Host-only logical backup. Credentials stay in an OS-protected MySQL option file. */
public final class BackupService {
    private final UnitOfWork transactions;private final AuthService auth;private final OutputSettings settings;
    private final AtomicBoolean running=new AtomicBoolean();
    public BackupService(UnitOfWork transactions,AuthService auth,OutputSettings settings){this.transactions=transactions;this.auth=auth;this.settings=settings;}
    public boolean enabled(){return settings.backupEnabled();}
    public Path outputDirectory(){return settings.backups();}
    private record Context(User actor,QueueClock clock) { }
    private Context authorize(Session session)throws ServiceException{return transactions.read(c->new Context(auth.require(c,session,Role.ADMIN),c.queue().clock()));}
    public BackupResult create(Session session)throws ServiceException {
        Context context=authorize(session);
        if(!settings.backupEnabled())throw new ValidationException("Backups are disabled on this computer. Use the configured host administrator workstation.");
        if(!running.compareAndSet(false,true))throw new ValidationException("A backup is already running on this workstation.");
        Path job=null;Process process=null;
        try {
            Path executable=settings.dumpExecutable(),options=settings.backupOptions();
            if(!executable.isAbsolute()||!options.isAbsolute()||!Files.isRegularFile(executable,LinkOption.NOFOLLOW_LINKS)||!Files.isRegularFile(options,LinkOption.NOFOLLOW_LINKS))throw new ValidationException("Configure the MySQL 8.4 mysqldump executable and protected backup option file on the host.");
            Files.createDirectories(settings.backups());job=Files.createDirectory(settings.backups().resolve("backup-"+UUID.randomUUID()));
            Path version=job.resolve("client-version.txt");
            process=new ProcessBuilder(executable.toString(),"--no-defaults","--version").redirectErrorStream(true).redirectOutput(version.toFile()).start();
            if(!process.waitFor(15,TimeUnit.SECONDS)){process.destroyForcibly();throw new ServiceException("The backup client version check timed out.");}
            String identity=Files.readString(version,StandardCharsets.UTF_8);
            if(process.exitValue()!=0||!identity.matches("(?s).*\\b8\\.4\\.[0-9]+.*")||identity.toLowerCase(Locale.ROOT).contains("mariadb"))throw new ValidationException("Use Oracle MySQL 8.4 mysqldump. The XAMPP/MariaDB backup client is not compatible with this deployment.");
            Path partial=job.resolve("backup.sql.partial"),errors=job.resolve("diagnostic.txt");
            // --defaults-file must be the first option. No shell, command interpolation or command-line password.
            List<String> arguments=List.of(executable.toString(),"--defaults-file="+options,"--ssl-mode=VERIFY_IDENTITY",
                "--single-transaction","--quick","--no-tablespaces","--set-gtid-purged=OFF","--skip-lock-tables","--triggers","--hex-blob",
                "--default-character-set=utf8mb4","--column-statistics=0","--skip-dump-date","--result-file="+partial,"--databases","queue_clearance");
            process=new ProcessBuilder(arguments).redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(errors.toFile()).start();
            if(!process.waitFor(settings.backupTimeoutSeconds(),TimeUnit.SECONDS)){process.destroyForcibly();throw new ServiceException("The backup timed out. Its incomplete folder must not be used for restoration.");}
            if(process.exitValue()!=0||!Files.isRegularFile(partial)||Files.size(partial)==0)throw new ServiceException("The backup failed. Ask the host administrator to inspect the backup diagnostic file.");
            authorize(session); // Revalidate the administrator before declaring the backup complete.
            String digest=sha256(partial);long bytes=Files.size(partial);Files.move(partial,job.resolve("backup.sql"));
            Properties manifest=new Properties();manifest.setProperty("database","queue_clearance");manifest.setProperty("startedAtServer",context.clock().time().toString());manifest.setProperty("completedAtServer",authorize(session).clock().time().toString());
            manifest.setProperty("actorUserId",Long.toString(context.actor().id()));manifest.setProperty("actorUsername",context.actor().username());manifest.setProperty("sha256",digest);manifest.setProperty("bytes",Long.toString(bytes));manifest.setProperty("schema","V008");
            // Properties.store adds a client timestamp: write deterministic escaped properties without that header.
            try(Writer writer=Files.newBufferedWriter(job.resolve("manifest.properties"),StandardCharsets.UTF_8)){for(String key:new TreeSet<>(manifest.stringPropertyNames()))writer.write(key+"="+escape(manifest.getProperty(key))+"\n");}
            Files.writeString(job.resolve("COMPLETE"),digest+"\n",StandardCharsets.US_ASCII,StandardOpenOption.CREATE_NEW);
            return new BackupResult(job,bytes,digest,context.clock().time());
        }catch(IOException ex){throw new ServiceException("Cannot create the backup. Check the configured paths, permissions and disk space.",ex);}
        catch(InterruptedException ex){Thread.currentThread().interrupt();throw new ServiceException("The backup was interrupted. Its incomplete folder must not be used for restoration.",ex);}
        finally {if(process!=null&&process.isAlive())process.destroyForcibly();running.set(false);}
    }
    private static String escape(String value){return value.replace("\\","\\\\").replace("\n","\\n").replace("\r","\\r").replace("=","\\=").replace(":","\\:");}
    public static String sha256(Path file)throws IOException {
        try {MessageDigest digest=MessageDigest.getInstance("SHA-256");try(InputStream input=Files.newInputStream(file)){byte[] buffer=new byte[65536];for(int n;(n=input.read(buffer))!=-1;)digest.update(buffer,0,n);}return HexFormat.of().formatHex(digest.digest());}
        catch(NoSuchAlgorithmException ex){throw new IllegalStateException("SHA-256 is required by Java",ex);}
    }
}
