package model;

import java.time.LocalDate;

public record TermDraft(String code, String name, LocalDate startsOn, LocalDate endsOn, boolean active, boolean current) {
}
