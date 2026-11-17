package ui.model;

import model.UserAccount;

public final class UserTableModel extends RecordTableModel<UserAccount> {
    public UserTableModel() { super("Username", "Display name", "Role", "Assignment", "Status", "Password change"); }
    @Override public Object getValueAt(int row, int column) {
        UserAccount a = rowAt(row);
        return switch (column) {
            case 0 -> a.user().username(); case 1 -> a.user().displayName(); case 2 -> a.user().role().displayName();
            case 3 -> a.assignment(); case 4 -> a.user().active() ? "Active" : "Inactive"; case 5 -> a.user().mustChangePassword() ? "Required" : "Not required";
            default -> throw new IndexOutOfBoundsException(column);
        };
    }
}
