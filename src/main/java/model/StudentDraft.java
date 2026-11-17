package model;

public record StudentDraft(String studentNumber, String firstName, String middleName,
        String lastName, String suffix, String program, int yearLevel, String section, boolean active) {
}
