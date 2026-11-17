package service;

public final class RecordChangedException extends ServiceException {
    public RecordChangedException() {
        super("This record changed on another workstation. Close the editor, refresh and try again.");
    }
}
