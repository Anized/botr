package org.anized.jafool.books.model;

import java.util.Optional;

public class ISBN13 {
    public static final String ISBN_TAG = "ISBN-13:";
    private final long code;

    public ISBN13(final String code) {
        this.code = Optional.ofNullable(code).map(String::trim)
                .filter(isbn -> isbn.toUpperCase().startsWith(ISBN_TAG))
                .map(isbn -> isbn.substring(ISBN_TAG.length()).trim().replaceAll("-", ""))
                .map(Long::parseLong)
                .orElseThrow(() -> new IllegalStateException(
                        "ISBN code cannot be empty or null and must be prefixed with 'ISBN-13:'"));
    }

    @Override
    public String toString() { return ISBN_TAG+code; }

}
