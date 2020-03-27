package org.anized.jafool.books;

import com.google.common.collect.ImmutableMap;
import io.vavr.control.Try;
import org.anized.jafool.books.model.ISBN13;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

public class BookServices {
    private static final Logger logger = LoggerFactory.getLogger(BookServices.class);
    private static ISBN13 key1 = new ISBN13("ISBN-13:9780316217644");
    private static Map<String,ISBN13> bookCatalog = ImmutableMap.<String, ISBN13>builder()
            .put("Babylon's Ashes", key1)
            .build();
    private static Map<ISBN13, BigDecimal> priceList = ImmutableMap.<ISBN13,BigDecimal>builder()
            .put(key1, BigDecimal.valueOf(371,2))
            .build();
    private static Map<ISBN13, String> authorCatalog = ImmutableMap.<ISBN13,String>builder()
            .put(key1, "S.A. Corey")
            .build();
    private static Map<ISBN13, LocalDate> publicationDates = ImmutableMap.<ISBN13,LocalDate>builder()
            .put(key1, LocalDate.parse("2016-12-01"))
            .build();


    public static ISBN13 isbnLookup(final String title) {
        return bookCatalog.get(title);
    }

    public static void publishedLookup(final Exchange exchange) {
        final ISBN13 isbn = exchange.getIn().getBody(ISBN13.class);
        final Properties props = exchange.getProperty("bookProps", Properties.class);
        props.put("published", publicationDates.get(isbn));
        pause("publishedLookup");
    }

    public static void priceLookup(final Exchange exchange) {
        final ISBN13 isbn = exchange.getIn().getBody(ISBN13.class);
        final Properties props = exchange.getProperty("bookProps", Properties.class);
        props.put("price", priceList.get(isbn));
        pause("priceLookup");
    }

    public static void authorLookup(final Exchange exchange) {
        final ISBN13 isbn = exchange.getIn().getBody(ISBN13.class);
        final Properties props = exchange.getProperty("bookProps", Properties.class);
        props.put("author", authorCatalog.get(isbn));
        pause("authorLookup");
    }

    private static final Random r = new Random();
    private static void pause(final String tag) {
        Try.of(() -> {
            final long ms = r.nextInt(2001);
            logger.info("{}: sleep for "+ms+"ms", tag);
            Thread.sleep(ms);
            return null;
        }).get();
    }
}
