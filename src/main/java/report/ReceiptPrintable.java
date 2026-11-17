package report;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import model.ReceiptData;

/** Native Java printing, after payment commit. Long content wraps and paginates. */
public final class ReceiptPrintable implements Printable {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ReceiptData data;
    private record Part(String left, String right, boolean bold) { }
    private record Line(String text, boolean bold, boolean right) { }

    public ReceiptPrintable(ReceiptData data) { this.data = java.util.Objects.requireNonNull(data); }

    public String previewText() {
        StringBuilder text = new StringBuilder();
        for (Part part : parts()) {
            text.append(part.left());
            if (part.right() != null) { text.append(": ").append(part.right()); }
            text.append('\n');
        }
        return text.toString();
    }

    private List<Part> parts() {
        List<Part> p = new ArrayList<>();
        p.add(new Part("Queue-Clearance", null, true));
        p.add(new Part("PAYMENT RECEIPT", null, true));
        p.add(new Part(data.payment().voidedAt() == null ? "RECORDED" : "VOID - PAYMENT CANCELLED", null, true));
        p.add(new Part("Receipt: " + data.payment().receiptReference(), null, true));
        p.add(new Part("Paid at: " + DATE.format(data.payment().paidAt()) + " (+08:00)", null, false));
        p.add(new Part("", null, false));
        p.add(new Part("Student: " + data.studentNumber(), null, true));
        p.add(new Part(data.studentName(), null, false));
        p.add(new Part("Office: " + data.officeCode() + " - " + data.officeName(), null, false));
        p.add(new Part("Term: " + data.termCode() + " - " + data.termName(), null, false));
        p.add(new Part("Fine reason: " + data.reason(), null, false));
        p.add(new Part("", null, false));
        p.add(new Part("Fine amount at payment", money(data.assessedAmount()), false));
        p.add(new Part("Amount received", money(data.payment().amount()), true));
        p.add(new Part("Fine balance after this payment", money(data.balanceAfter()), false));
        p.add(new Part("", null, false));
        p.add(new Part("Received by: " + data.receiverName() + " (" + data.receiverUsername() + ")", null, false));
        if (data.payment().voidedAt() != null) {
            p.add(new Part("Voided at: " + DATE.format(data.payment().voidedAt()) + " (+08:00)", null, true));
            p.add(new Part("Void reason: " + data.payment().voidReason(), null, true));
        }
        p.add(new Part("Keep this receipt for your records.", null, false));
        return List.copyOf(p);
    }

    private static String money(BigDecimal amount) {
        DecimalFormat format = new DecimalFormat("'\u20b1' #,##0.00", DecimalFormatSymbols.getInstance(Locale.ROOT));
        return format.format(amount);
    }

    @Override public int print(Graphics graphics, PageFormat page, int pageIndex) throws PrinterException {
        if (pageIndex < 0) { return NO_SUCH_PAGE; }
        double width = page.getImageableWidth() - 24;
        double height = page.getImageableHeight() - 24;
        if (width < 96 || height < 64) { throw new PrinterException("Select a larger printable page area."); }
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.translate(page.getImageableX() + 12, page.getImageableY() + 12);
            g.setColor(Color.BLACK);
            Font regular = new Font("Segoe UI", Font.PLAIN, 12), bold = regular.deriveFont(Font.BOLD);
            List<Line> lines = new ArrayList<>();
            // Identity/status repeat on every printed page, including continuations.
            List<String> header = new ArrayList<>(); FontMetrics headerMetrics = g.getFontMetrics(bold);
            for (String text : List.of("Queue-Clearance", data.payment().voidedAt() == null ? "PAYMENT RECEIPT - RECORDED" : "VOID - PAYMENT CANCELLED", "Receipt: " + data.payment().receiptReference())) {
                header.addAll(wrap(text, headerMetrics, width));
            }
            for (Part part : parts().subList(4, parts().size())) {
                FontMetrics metrics = g.getFontMetrics(part.bold() ? bold : regular);
                if (part.right() != null && metrics.stringWidth(part.left() + "  " + part.right()) <= width) {
                    // Align numeric values against the right edge using a separate draw.
                    lines.add(new Line(part.left() + "\t" + part.right(), part.bold(), false));
                } else {
                    for (String line : wrap(part.left(), metrics, width)) { lines.add(new Line(line, part.bold(), false)); }
                    if (part.right() != null) { for (String line : wrap(part.right(), metrics, width)) { lines.add(new Line(line, part.bold(), true)); } }
                }
            }
            int lineHeight = Math.max(g.getFontMetrics(regular).getHeight(), g.getFontMetrics(bold).getHeight()) + 4;
            int pageLines = (int) height / lineHeight - header.size() - 2;
            if (pageLines < 1) { throw new PrinterException("Select a taller printable page area."); }
            if ((long) pageIndex * pageLines >= lines.size()) { return NO_SUCH_PAGE; }
            g.setFont(bold);
            for (int i = 0; i < header.size(); i++) { g.drawString(header.get(i), 0, i * lineHeight + headerMetrics.getAscent()); }
            int bodyTop = (header.size() + 1) * lineHeight;
            int start = pageIndex * pageLines, end = Math.min(lines.size(), start + pageLines);
            for (int i = start; i < end; i++) {
                Line line = lines.get(i); g.setFont(line.bold() ? bold : regular);
                int baseline = bodyTop + (i - start) * lineHeight + g.getFontMetrics().getAscent();
                int tab = line.text().indexOf('\t');
                if (tab >= 0) {
                    String left = line.text().substring(0, tab), right = line.text().substring(tab + 1);
                    g.drawString(left, 0, baseline); g.drawString(right, (float) (width - g.getFontMetrics().stringWidth(right)), baseline);
                } else { g.drawString(line.text(), line.right() ? (float) (width - g.getFontMetrics().stringWidth(line.text())) : 0, baseline); }
            }
            g.setFont(regular.deriveFont(10f));
            g.drawString("Page " + (pageIndex + 1) + " of " + ((lines.size() + pageLines - 1) / pageLines), 0, (float) (height - g.getFontMetrics().getDescent()));
            return PAGE_EXISTS;
        } finally { g.dispose(); }
    }

    private static List<String> wrap(String text, FontMetrics metrics, double width) {
        if (text == null || text.isEmpty()) { return List.of(""); }
        List<String> result = new ArrayList<>(); String remaining = text;
        while (!remaining.isEmpty()) {
            int end = 0, lastSpace = -1;
            while (end < remaining.length()) {
                int next = end + Character.charCount(remaining.codePointAt(end));
                if (metrics.stringWidth(remaining.substring(0, next)) > width && end > 0) { break; }
                if (Character.isWhitespace(remaining.codePointAt(end))) { lastSpace = end; }
                end = next;
            }
            if (end < remaining.length() && lastSpace > 0) { end = lastSpace; }
            result.add(remaining.substring(0, end).stripTrailing()); remaining = remaining.substring(end).stripLeading();
        }
        return result;
    }
}
