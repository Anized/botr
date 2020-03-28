package org.anized.jafool.books.model;

import java.util.Optional;

public class ISBN13 {
    public static final String ISBN_TAG = "ISBN-13:";
    private final long code;

    public ISBN13(final String code) {
        this.code = Optional.ofNullable(code)
                .filter(isbn -> !isbn.trim().isEmpty() && isbn.toUpperCase().startsWith(ISBN_TAG))
                .map(isbn -> Long.parseLong(isbn.split(":")[1].trim().replaceAll("-", "")))
                .orElseThrow(() ->
                        new IllegalStateException(
                                "ISBN code cannot be empty or null and must be prefixed with 'ISBN-13:'"));
    }

    @Override
    public String toString() { return String.format("%s%d", ISBN_TAG, code); }

}
