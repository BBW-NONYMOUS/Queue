package service;
import dao.*;
import model.*;
import config.OutputSettings;
import report.ReportWriter;
import java.nio.file.*;
import java.time.*;
import java.util.*;
public final class ReportService {
    private final UnitOfWork transactions;private final AuthService auth;private final OutputSettings outputs;
    public ReportService(UnitOfWork transactions,AuthService auth,OutputSettings outputs){this.transactions=transactions;this.auth=auth;this.outputs=outputs;}
    public Path outputDirectory(){return outputs.reports();}
    public FinanceReferences references(Session session)throws ServiceException {return transactions.read(c->{User actor=auth.require(c,session,Role.values());return new FinanceReferences(c.organizations().list().stream().filter(o->actor.role()!=Role.OFFICER||Objects.equals(actor.orgId(),o.id())).toList(),c.finance().terms());});}
    public ReportData preview(Session session,ReportFilter filter)throws ServiceException {
        ReportFilter clean=validate(filter);
        return transactions.snapshot(c->{User actor=auth.require(c,session,Role.values());ReportFilter scoped=scope(c,actor,clean);return c.oversight().report(scoped);});
    }
    public Path export(Session session,ReportFilter filter,ExportFormat format)throws ServiceException {
        if(format==null)throw new ValidationException("Choose PDF or Excel.");ReportData data=preview(session,filter);Path temporary=null;
        try {
            Files.createDirectories(outputs.reports());temporary=Files.createTempFile(outputs.reports(),".export-",".partial");
            new ReportWriter(outputs.pdfFont()).write(data,format,temporary);
            // A revoked/reassigned account must not publish a completed export.
            transactions.read(c->{User actor=auth.require(c,session,Role.values());scope(c,actor,validate(filter));return null;});
            Path destination=outputs.reports().resolve(filter.type().name().toLowerCase(Locale.ROOT)+"-"+UUID.randomUUID()+"."+format.extension());
            Files.move(temporary,destination);temporary=null;return destination;
        } catch(java.io.IOException ex){throw new ServiceException("The report could not be written. Check the output folder and available disk space.",ex);}
        finally {if(temporary!=null)try{Files.deleteIfExists(temporary);}catch(java.io.IOException ex){org.slf4j.LoggerFactory.getLogger(ReportService.class).warn("Incomplete report cleanup failed",ex);}}
    }
    private static ReportFilter validate(ReportFilter f)throws ServiceException {
        if(f==null||f.type()==null)throw new ValidationException("Choose a report.");dates(f.from(),f.through());
        if(f.yearLevel()!=null&&(f.yearLevel()<1||f.yearLevel()>6))throw new ValidationException("Choose year level 1 to 6.");
        if(f.type()==ReportType.CLEARANCE&&f.termId()==null)throw new ValidationException("Choose one term for clearance reports.");
        if((f.type()==ReportType.STUDENTS||f.type()==ReportType.CLEARANCE)&&(f.from()!=null||f.through()!=null))throw new ValidationException("Date filters apply to queue, fine assessment and payment reports.");
        if(f.type()==ReportType.STUDENTS&&(f.termId()!=null||f.orgId()!=null))throw new ValidationException("Student roster reports use program, year and section filters instead of office or term.");
        return new ReportFilter(f.type(),f.termId(),f.orgId(),Validation.filter(f.studentNumber(),"Student number",32),Validation.filter(f.program(),"Program",80),f.yearLevel(),Validation.filter(f.section(),"Section",32),f.from(),f.through(),f.includeInactive());
    }
    static void dates(LocalDate from,LocalDate through)throws ValidationException {
        if(from!=null&&(from.getYear()<1000||from.getYear()>9998)||through!=null&&(through.getYear()<1000||through.getYear()>9998)||from!=null&&through!=null&&through.isBefore(from))throw new ValidationException("Enter dates from year 1000 to 9998, with To on or after From.");
    }
    private static ReportFilter scope(DaoContext c,User actor,ReportFilter f)throws ServiceException {
        if(actor.role()==Role.STUDENT&&f.type()!=ReportType.CLEARANCE)throw new AuthorizationException("Students can export only their own clearance report.");
        if(actor.role()==Role.OFFICER&&f.type()==ReportType.PAYMENTS)throw new AuthorizationException("Payment reports are available to administrators and cashiers.");
        if(actor.role()==Role.CASHIER&&f.type()==ReportType.QUEUE)throw new AuthorizationException("Queue detail reports are available to administrators and officers.");
        Long office=f.orgId();String number=f.studentNumber();
        if(actor.role()==Role.OFFICER&&f.type()!=ReportType.STUDENTS){if(office!=null&&!Objects.equals(office,actor.orgId()))throw new AuthorizationException("Use your assigned office's reports.");office=actor.orgId();}
        if(actor.role()==Role.STUDENT)number=c.students().findById(actor.studentId()).orElseThrow(()->new NotFoundException("Student")).studentNumber();
        if(f.termId()!=null)c.finance().findTerm(f.termId()).orElseThrow(()->new NotFoundException("Term"));
        if(office!=null)c.organizations().findById(office).orElseThrow(()->new NotFoundException("Office"));
        return new ReportFilter(f.type(),f.termId(),office,number,f.program(),f.yearLevel(),f.section(),f.from(),f.through(),f.includeInactive());
    }
}
