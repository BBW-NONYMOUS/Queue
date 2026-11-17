package ui.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import model.*;
import service.ValidationException;
import ui.model.IdChoice;

public final class FinanceUi {
    private FinanceUi() { }
    public static void offices(JComboBox<IdChoice> box, List<Organization> offices, boolean all) {
        DefaultComboBoxModel<IdChoice> model = new DefaultComboBoxModel<>();
        if (all) { model.addElement(new IdChoice(null, "All offices")); }
        for (Organization o : offices) { model.addElement(new IdChoice(o.id(), o.code().name() + " - " + o.name() + (o.active() ? "" : " (inactive)"))); }
        box.setModel(model); plain(box);
    }
    public static void terms(JComboBox<IdChoice> box, List<Term> terms, boolean all) {
        DefaultComboBoxModel<IdChoice> model = new DefaultComboBoxModel<>();
        if (all) { model.addElement(new IdChoice(null, "All terms")); }
        for (Term t : terms) { model.addElement(new IdChoice(t.id(), t.code() + (t.active() ? "" : " (inactive)") + (t.current() ? " - current" : ""))); }
        box.setModel(model); plain(box);
    }
    private static void plain(JComboBox<IdChoice> box) { DefaultListCellRenderer renderer = new DefaultListCellRenderer(); renderer.putClientProperty("html.disable", Boolean.TRUE); box.setRenderer(renderer); }
    public static Long id(JComboBox<IdChoice> box) { IdChoice c = (IdChoice) box.getSelectedItem(); return c == null ? null : c.id(); }
    public static void select(JComboBox<IdChoice> box, Long id) {
        for (int i = 0; i < box.getItemCount(); i++) { if (java.util.Objects.equals(box.getItemAt(i).id(), id)) { box.setSelectedIndex(i); return; } }
    }
    public static LocalDate date(String value) throws ValidationException {
        if (value == null || value.isBlank()) { return null; }
        try { return LocalDate.parse(value.strip()); } catch (DateTimeParseException ex) { throw new ValidationException("Enter dates as YYYY-MM-DD, such as 2026-11-17."); }
    }
}
