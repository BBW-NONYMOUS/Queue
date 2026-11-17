package service;

public final class NotFoundException extends ServiceException {
    public NotFoundException(String record) { super(record + " was not found. Refresh and try again."); }
}
