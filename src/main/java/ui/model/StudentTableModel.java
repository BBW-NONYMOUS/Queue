package ui.model;

import model.Student;

public final class StudentTableModel extends RecordTableModel<Student> {
    public StudentTableModel() { super("Student number", "Name", "Program", "Year", "Section", "Status"); }
    @Override public Class<?> getColumnClass(int column) { return column == 3 ? Integer.class : String.class; }
    @Override public Object getValueAt(int row, int column) {
        Student s = rowAt(row);
        return switch (column) {
            case 0 -> s.studentNumber();
            case 1 -> String.join(" ", java.util.stream.Stream.of(s.firstName(), s.middleName(), s.lastName(), s.suffix()).filter(v -> v != null && !v.isBlank()).toList());
            case 2 -> s.program(); case 3 -> s.yearLevel(); case 4 -> s.section(); case 5 -> s.active() ? "Active" : "Inactive";
            default -> throw new IndexOutOfBoundsException(column);
        };
    }
}
