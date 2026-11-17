package model;

/** Display-ready account assignment without exposing database keys or credentials. */
public record AccountProfile(User user, String assignment) {
}
