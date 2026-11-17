package ui.model;

public record IdChoice(Long id, String label) {
    @Override public String toString() { return label; }
}
