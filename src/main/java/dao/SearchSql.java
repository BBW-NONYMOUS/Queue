package dao;

/** Literal substring matching; users cannot accidentally enter LIKE wildcards. */
final class SearchSql {
    private SearchSql() {
    }

    static String contains(String value) {
        return "%" + value.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }
}
