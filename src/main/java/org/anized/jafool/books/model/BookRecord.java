package org.anized.jafool.books.model;

import com.google.common.base.Objects;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookRecord {
    private final ISBN13 isbnCode;
    private final String author;
    private final LocalDate published;
    private final String title;
    private final BigDecimal price;

    public BookRecord(final ISBN13 bookCode, final String author, final LocalDate published, final String title, final BigDecimal price) {
        this.isbnCode = bookCode;
        this.published = published;
        this.author = author;
        this.title = title;
        this.price = price;
    }

    public String getAuthor() {
        return author;
    }
    public String getTitle() {
        return title;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public ISBN13 getIsbnCode() {
        return isbnCode;
    }
    public LocalDate getPublished() {
        return published;
    }

    @Override
    public String toString() {
        return "BookRecord{" +
                "isbnCode=" + isbnCode +
                ", author='" + author + '\'' +
                ", published='" + published + '\'' +
                ", title='" + title + '\'' +
                ", price=" + price +
                '}';
    }

}
