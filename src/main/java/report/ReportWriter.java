package report;
import model.*;
import service.ServiceException;
import java.io.*;
import java.nio.file.*;
import java.math.BigDecimal;
import org.openpdf.text.*;
import org.openpdf.text.pdf.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.*;
/** Renders an already-authorized immutable snapshot. No database access. */
public final class ReportWriter {
    private final Path fontFile;
    public ReportWriter(Path fontFile){this.fontFile=fontFile;}
    public void write(ReportData data,ExportFormat format,Path destination)throws ServiceException,IOException {
        if(javax.swing.SwingUtilities.isEventDispatchThread())throw new IllegalStateException("Export reports in a SwingWorker.");
        if(format==ExportFormat.PDF)pdf(data,destination);else excel(data,destination);
    }
    private void pdf(ReportData data,Path destination)throws ServiceException,IOException {
        if(!Files.isRegularFile(fontFile))throw new ServiceException("The PDF font file is missing. Configure report.font.file with a Unicode TrueType font.");
        Document document=new Document(PageSize.A4.rotate(),24,24,28,32);
        try(OutputStream stream=Files.newOutputStream(destination)){
            try {
            BaseFont base=BaseFont.createFont(fontFile.toString(),BaseFont.IDENTITY_H,BaseFont.EMBEDDED);
            org.openpdf.text.Font body=new org.openpdf.text.Font(base,8.5f),heading=new org.openpdf.text.Font(base,16,org.openpdf.text.Font.BOLD);
            PdfWriter writer=PdfWriter.getInstance(document,stream);
            writer.setPageEvent(new PdfPageEventHelper(){@Override public void onEndPage(PdfWriter w,Document d){ColumnText.showTextAligned(w.getDirectContent(),Element.ALIGN_RIGHT,new Phrase("Queue-Clearance | Page "+w.getPageNumber(),body),d.right(),16,0);}});
            document.addTitle(data.title());document.addAuthor("Queue-Clearance");document.open();
            java.util.Calendar serverCalendar=java.util.GregorianCalendar.from(data.generatedAt().atZone(java.time.ZoneOffset.ofHours(8)));
            writer.getInfo().put(PdfName.CREATIONDATE,new PdfDate(serverCalendar));writer.getInfo().put(PdfName.MODDATE,new PdfDate(serverCalendar));
            document.add(new Paragraph(data.title(),heading));
            Paragraph details=new Paragraph("Snapshot (server +08:00): "+data.generatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+". Rows: "+data.rows().size()+"\n"+data.scope(),body);details.setSpacingAfter(12);document.add(details);
            PdfPTable table=new PdfPTable(data.columns().size());table.setWidthPercentage(100);table.setHeaderRows(1);table.setSplitLate(false);table.setSplitRows(true);
            float[] widths=new float[data.columns().size()];for(int i=0;i<widths.length;i++)widths[i]=data.columns().get(i).label().contains("reason")||data.columns().get(i).label().equals("Reason")?2.2f:1f;table.setWidths(widths);
            for(var column:data.columns()){PdfPCell cell=new PdfPCell(new Phrase(column.label(),new org.openpdf.text.Font(base,8.5f,org.openpdf.text.Font.BOLD,java.awt.Color.WHITE)));cell.setBackgroundColor(new java.awt.Color(0x175CD3));cell.setPadding(5);table.addCell(cell);}
            for(int row=0;row<data.rows().size();row++)for(int col=0;col<data.columns().size();col++){
                Object value=data.rows().get(row).get(col);String text=value instanceof BigDecimal money?"\u20b1 "+new java.text.DecimalFormat("#,##0.00",java.text.DecimalFormatSymbols.getInstance(java.util.Locale.ROOT)).format(money):value==null?"":value instanceof java.time.LocalDateTime timestamp?timestamp.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")):String.valueOf(value);
                PdfPCell cell=new PdfPCell(new Phrase(text,body));cell.setPadding(5);cell.setVerticalAlignment(Element.ALIGN_TOP);cell.setHorizontalAlignment(data.columns().get(col).money()?Element.ALIGN_RIGHT:Element.ALIGN_LEFT);
                if(row%2==1)cell.setBackgroundColor(new java.awt.Color(0xF2F4F7));table.addCell(cell);
            }
            document.add(table);if(data.rows().isEmpty())document.add(new Paragraph("No matching records.",body));
            } finally {if(document.isOpen())document.close();}
        }catch(DocumentException ex){throw new ServiceException("The PDF could not be generated. Check the configured font.",ex);}
    }
    private void excel(ReportData data,Path destination)throws IOException,ServiceException {
        try(XSSFWorkbook workbook=new XSSFWorkbook()){
            var core=workbook.getProperties().getCoreProperties();var instant=java.util.Date.from(data.generatedAt().toInstant(java.time.ZoneOffset.ofHours(8)));core.setCreated(java.util.Optional.of(instant));core.setModified(java.util.Optional.of(instant));core.setCreator("Queue-Clearance");core.setLastModifiedByUser("Queue-Clearance");
            var sheet=workbook.createSheet("Report");int count=data.columns().size();sheet.setDisplayGridlines(false);
            org.apache.poi.ss.usermodel.Font normal=workbook.createFont();normal.setFontName("Segoe UI");normal.setFontHeightInPoints((short)11);
            org.apache.poi.ss.usermodel.Font bold=workbook.createFont();bold.setFontName("Segoe UI");bold.setBold(true);bold.setColor(IndexedColors.WHITE.getIndex());
            CellStyle body=workbook.createCellStyle();body.setFont(normal);body.setWrapText(true);body.setVerticalAlignment(VerticalAlignment.TOP);
            CellStyle money=workbook.createCellStyle();money.cloneStyleFrom(body);money.setAlignment(HorizontalAlignment.RIGHT);money.setDataFormat(workbook.createDataFormat().getFormat("\"₱\" #,##0.00"));
            CellStyle date=workbook.createCellStyle();date.cloneStyleFrom(body);date.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));
            CellStyle timestamp=workbook.createCellStyle();timestamp.cloneStyleFrom(body);timestamp.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
            CellStyle integer=workbook.createCellStyle();integer.cloneStyleFrom(body);integer.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            CellStyle header=workbook.createCellStyle();header.setFont(bold);header.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());header.setFillPattern(FillPatternType.SOLID_FOREGROUND);header.setWrapText(true);
            org.apache.poi.ss.usermodel.Font titleFont=workbook.createFont();titleFont.setFontName("Segoe UI");titleFont.setFontHeightInPoints((short)16);titleFont.setBold(true);CellStyle titleStyle=workbook.createCellStyle();titleStyle.setFont(titleFont);
            sheet.createRow(0).createCell(0).setCellValue(data.title());sheet.getRow(0).getCell(0).setCellStyle(titleStyle);sheet.getRow(0).setHeightInPoints(26);if(count>1)sheet.addMergedRegion(new CellRangeAddress(0,0,0,count-1));
            sheet.createRow(1).createCell(0).setCellValue("Snapshot (server +08:00): "+data.generatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+". Rows: "+data.rows().size());sheet.getRow(1).getCell(0).setCellStyle(body);sheet.getRow(1).setHeightInPoints(22);if(count>1)sheet.addMergedRegion(new CellRangeAddress(1,1,0,count-1));
            sheet.createRow(2).createCell(0).setCellValue(data.scope());sheet.getRow(2).getCell(0).setCellStyle(body);sheet.getRow(2).setHeightInPoints(64);if(count>1)sheet.addMergedRegion(new CellRangeAddress(2,2,0,count-1));
            var headings=sheet.createRow(4);headings.setHeightInPoints(32);for(int c=0;c<count;c++){var cell=headings.createCell(c);cell.setCellValue(data.columns().get(c).label());cell.setCellStyle(header);sheet.setColumnWidth(c,(data.columns().get(c).label().toLowerCase(java.util.Locale.ROOT).contains("reason")?42:23)*256);}
            for(int r=0;r<data.rows().size();r++){var row=sheet.createRow(r+5);int lines=2;for(int c=0;c<count;c++){int width=sheet.getColumnWidth(c)/256;String text=String.valueOf(data.rows().get(r).get(c));lines=Math.max(lines,(text.length()+width-1)/width+(int)text.chars().filter(ch->ch==10).count());}row.setHeightInPoints(Math.min(409,lines*14+8));for(int c=0;c<count;c++){
                var cell=row.createCell(c);Object value=data.rows().get(r).get(c);
                if(value==null){cell.setBlank();cell.setCellStyle(body);}
                else if(value instanceof BigDecimal amount){if(amount.precision()>15)throw new ServiceException("An amount exceeds Excel numeric precision. Export PDF or narrow the report.");cell.setCellValue(amount.doubleValue());cell.setCellStyle(money);}else if(value instanceof java.time.LocalDate day){cell.setCellValue(day);cell.setCellStyle(date);}
                else if(value instanceof java.time.LocalDateTime time){cell.setCellValue(time);cell.setCellStyle(timestamp);}
                else if(value instanceof Number number){cell.setCellValue(number.doubleValue());cell.setCellStyle(integer);}
                else{String text=String.valueOf(value);cell.setCellValue(text);cell.setCellStyle(body);}
            }}
            sheet.createFreezePane(0,5);sheet.setAutoFilter(new CellRangeAddress(4,Math.max(4,data.rows().size()+4),0,count-1));
            sheet.getPrintSetup().setLandscape(true);sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);sheet.setFitToPage(true);sheet.getPrintSetup().setFitWidth((short)1);sheet.getPrintSetup().setFitHeight((short)0);sheet.setRepeatingRows(new CellRangeAddress(4,4,-1,-1));
            try(OutputStream stream=Files.newOutputStream(destination)){workbook.write(stream);}
        }
    }
}
