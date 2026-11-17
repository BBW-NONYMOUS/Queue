package service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** PHP amounts use exact decimal arithmetic; never silently round an entry. */
public final class Money {
    private static final BigDecimal MAXIMUM = new BigDecimal("9999999999.99");
    private Money() { }

    public static BigDecimal parse(String input) throws ValidationException {
        String value = input == null ? "" : input.strip();
        if (!value.matches("[0-9]{1,10}(\\.[0-9]{1,2})?")) {
            throw new ValidationException("Enter a positive PHP amount with at most two decimals, without commas.");
        }
        return positive(new BigDecimal(value));
    }

    public static BigDecimal positive(BigDecimal input) throws ValidationException {
        if (input == null) { throw new ValidationException("Enter the amount."); }
        try {
            BigDecimal value = input.setScale(2, RoundingMode.UNNECESSARY);
            if (value.signum() <= 0 || value.compareTo(MAXIMUM) > 0) { throw new ArithmeticException(); }
            return value;
        } catch (ArithmeticException ex) {
            throw new ValidationException("Use a PHP amount from 0.01 to 9,999,999,999.99 with at most two decimals.");
        }
    }
}
