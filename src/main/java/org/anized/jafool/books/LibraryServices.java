package org.anized.jafool.books;

import com.google.common.collect.ImmutableMap;
import io.vavr.Function2;
import io.vavr.control.Try;
import org.anized.jafool.books.model.ISBN13;
import org.apache.camel.Exchange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.function.Function;

public class LibraryServices {
    private static final ISBN13 key1 = new ISBN13("ISBN-13:9780316217644");
    private static final Map<String, ISBN13> bookCatalog =
            ImmutableMap.<String, ISBN13>builder().put("Babylon's Ashes", key1).build();
    private static final Map<ISBN13, BigDecimal> priceList =
            ImmutableMap.<ISBN13, BigDecimal>builder().put(key1, BigDecimal.valueOf(371, 2)).build();
    private static final Map<ISBN13, String> authorCatalog =
            ImmutableMap.<ISBN13, String>builder().put(key1, "S.A. Corey").build();
    private static final Map<ISBN13, LocalDate> publicationDates =
            ImmutableMap.<ISBN13, LocalDate>builder().put(key1, LocalDate.parse("2016-12-01")).build();

    public void setIsbnCode(final Exchange exchange) {
        final String title = exchange.getIn().getBody(String.class);
        Optional.ofNullable(bookCatalog.get(exchange.getIn().getBody(String.class)))
                .map(isbn -> mapCodeAndTitle.apply(isbn, title))
                .map(props -> {
                    exchange.getMessage().setBody(props);
                    return true;
                })
                .orElseThrow(() -> new IllegalStateException(
                        "failed to lookup ISBN code for title '" + title + "'"));
    }

    private static final Function2<ISBN13,String,Properties> mapCodeAndTitle = (isbn, title) -> {
        final Properties result = new Properties();
        result.putAll(ImmutableMap.<String, Object>builder()
                .put("isbn", isbn)
                .put("title", title)
                .build());
        return result;
    };

    public void publishedLookup(final Exchange exchange) {
        query(exchange, "published", publicationDates::get);
    }

    public void priceLookup(final Exchange exchange) {
        query(exchange, "price", priceList::get);
    }

    public void authorLookup(final Exchange exchange) {
        query(exchange, "author", authorCatalog::get);
    }

    private static void query(final Exchange exchange, final String field,
                              final Function<ISBN13, Object> lookup) {
        final Properties props = exchange.getIn().getBody(Properties.class);
        props.put(field, lookup.apply((ISBN13) props.get("isbn")));
        exchange.getMessage().setBody(props);
        randomPause();
    }

    private static void randomPause() {
        Try.of(() -> {
            final long ms = (new Random())
                    .ints(0, 2001).findFirst().orElse(0);
            Thread.sleep(ms);
            return null;
        }).get();
    }
}