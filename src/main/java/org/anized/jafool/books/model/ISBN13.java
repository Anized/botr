package org.anized.jafool.books.model;

public class ISBN13 {
    public static final String ISBN_13 = "ISBN-13:";
    private final long code;

    public ISBN13(final String code) {
        if(code == null || code.trim().isEmpty()
            || !code.toUpperCase().startsWith(ISBN_13)) {
            throw new IllegalStateException(
                    "ISBN code cannot be empty or null and must be prefixed with 'ISBN-13:'");
        }
        this.code = Long.parseLong(code.split(":")[1].trim().replaceAll("-",""));
    }

    @Override
    public String toString() {
        return String.format("%s%d", ISBN_13, code);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ISBN13 isbn13 = (ISBN13) o;
        return code == isbn13.code;
    }

}
