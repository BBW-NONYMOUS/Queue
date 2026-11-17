package model;

import java.util.List;

public record FinanceReferences(List<Organization> offices, List<Term> terms) {
    public FinanceReferences { offices = List.copyOf(offices); terms = List.copyOf(terms); }
}
