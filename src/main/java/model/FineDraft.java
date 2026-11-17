package model;

import java.math.BigDecimal;

public record FineDraft(String studentNumber, long orgId, long termId, String reason, BigDecimal amount) {
}
