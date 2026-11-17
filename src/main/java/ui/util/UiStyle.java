package ui.util;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.swing.JLabel;

public final class UiStyle {
    public static final Color PRIMARY = new Color(0x175CD3);
    public static final Color ERROR = new Color(0xB42318);
    public static final Color SUCCESS = new Color(0x067647);
    public static final Color MUTED = new Color(0x475467);

    private UiStyle() {
    }

    public static void status(JLabel label, String message, boolean error) {
        label.putClientProperty("html.disable", Boolean.FALSE);
        label.setForeground(error ? ERROR : SUCCESS);
        label.setText("<html>" + escape(message) + "</html>");
        label.getAccessibleContext().setAccessibleDescription(message);
    }

    public static void primary(javax.swing.JButton button) {
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.putClientProperty("JButton.buttonType", "roundRect");
    }

    public static void heading(JLabel label) {
        label.setFont(label.getFont().deriveFont(Font.BOLD, 22f));
    }

    public static void plainText(JLabel label, String value) {
        // Student numbers and usernames are data, not Swing HTML instructions.
        label.putClientProperty("html.disable", Boolean.TRUE);
        label.setText(value);
    }

    public static String pesos(java.math.BigDecimal amount) {
        DecimalFormat format = new DecimalFormat("'\u20b1' #,##0.00", DecimalFormatSymbols.getInstance(Locale.ROOT));
        format.setRoundingMode(java.math.RoundingMode.HALF_UP);
        return format.format(amount);
    }

    public static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
