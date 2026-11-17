package report;
import model.*;import service.ServiceException;import java.nio.file.*;import java.math.BigDecimal;import java.time.*;import java.util.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;import org.apache.poi.ss.usermodel.*;import org.openpdf.text.pdf.PdfReader;
import org.junit.jupiter.api.*;import org.junit.jupiter.api.io.TempDir;import static org.junit.jupiter.api.Assertions.*;
class ReportWriterTest {
    @TempDir Path temporary;
    private final LocalDateTime clock=LocalDateTime.of(2026,11,17,9,0);
    private Path font(){return Path.of(System.getProperty("queueclearance.test.font","C:/Windows/Fonts/segoeui.ttf"));}
    private ReportData sample(){return new ReportData("Validation report",clock,"Synthetic test data",List.of(new ReportColumn("Student",false),new ReportColumn("Balance (PHP)",true),new ReportColumn("Date",ReportValueType.DATE),new ReportColumn("Completed",ReportValueType.DATETIME)),List.of(Arrays.asList("=HYPERLINK(\"not-a-link\")",new BigDecimal("1234.56"),clock.toLocalDate(),null)));}
    @Test void excelPreservesTypesAndFormulaLikeStrings()throws Exception {
        Path output=temporary.resolve("report.xlsx");new ReportWriter(font()).write(sample(),ExportFormat.EXCEL,output);
        try(var input=Files.newInputStream(output);var book=new XSSFWorkbook(input)){
            var sheet=book.getSheetAt(0);var row=sheet.getRow(5);assertEquals(CellType.STRING,row.getCell(0).getCellType());assertEquals("=HYPERLINK(\"not-a-link\")",row.getCell(0).getStringCellValue());
            assertEquals(CellType.NUMERIC,row.getCell(1).getCellType());assertEquals(1234.56,row.getCell(1).getNumericCellValue());assertTrue(row.getCell(1).getCellStyle().getDataFormatString().contains("₱"));assertEquals(HorizontalAlignment.RIGHT,row.getCell(1).getCellStyle().getAlignment());
            assertTrue(DateUtil.isCellDateFormatted(row.getCell(2)));assertEquals(CellType.BLANK,row.getCell(3).getCellType());assertTrue(sheet.getPaneInformation().isFreezePane());assertEquals(clock.toInstant(ZoneOffset.ofHours(8)),book.getProperties().getCoreProperties().getCreated().toInstant());
        }
    }
    @Test void pdfIsReadableAndUsesServerCreationDate()throws Exception {
        Assumptions.assumeTrue(Files.isRegularFile(font()),"Configure queueclearance.test.font for PDF tests on this OS");
        Path output=temporary.resolve("report.pdf");new ReportWriter(font()).write(sample(),ExportFormat.PDF,output);PdfReader reader=new PdfReader(Files.readAllBytes(output));try{assertEquals(1,reader.getNumberOfPages());assertTrue(reader.getInfo().get("CreationDate").contains("20261117"));}finally{reader.close();}
    }
    @Test void missingFontProducesPlainTypedError(){assertThrows(ServiceException.class,()->new ReportWriter(temporary.resolve("missing.ttf")).write(sample(),ExportFormat.PDF,temporary.resolve("report.pdf")));}
    @Test void refusesAmountsBeyondExcelPrecision(){var report=new ReportData("Large amount",clock,"Test",List.of(new ReportColumn("Amount",true)),List.of(List.of(new BigDecimal("1234567890123456.00"))));assertThrows(ServiceException.class,()->new ReportWriter(font()).write(report,ExportFormat.EXCEL,temporary.resolve("too-large.xlsx")));}
    @Test void reportSnapshotDefensivelyCopiesRowsAndRetainsNulls(){var row=new ArrayList<Object>(Arrays.asList("Original",null));var report=new ReportData("Test",clock,"Test",List.of(new ReportColumn("One",false),new ReportColumn("Two",false)),List.of(row));row.set(0,"Changed");assertEquals("Original",report.rows().getFirst().getFirst());assertNull(report.rows().getFirst().get(1));assertThrows(UnsupportedOperationException.class,()->report.rows().getFirst().set(0,"Mutated"));}
}
