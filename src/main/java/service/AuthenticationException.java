package service;

public final class AuthenticationException extends ServiceException {
    public AuthenticationException() {
        super("The username or password is incorrect, or the account is inactive.");
    }
}
