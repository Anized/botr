package org.anized.jafool;

import org.anized.jafool.books.*;
import org.anized.jafool.books.model.BookRecord;
import org.anized.jafool.books.model.ISBN13;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ThreadPoolBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

public class ParallelRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        final ExecutorService ec =
                new ThreadPoolBuilder(getContext())
                        .poolSize(10)
                        .maxPoolSize(150)
                        .maxQueueSize(150)
                        .build("book-processor-pool");
        getContext().getTypeConverterRegistry()
                .addTypeConverters(new TypeConverters());

        from("direct:parallel")
                .convertBodyTo(ISBN13.class)
                .multicast()
                .parallelProcessing(true)
                .executorService(ec)
                .aggregationStrategy(new MergeHub())
                    .to("direct:published-query")
                    .to("direct:price-query")
                    .to("direct:author-query")
                .end()
                .convertBodyTo(BookRecord.class);

        from("direct:published-query").routeId("published-lookup")
                .process(BookServices::publishedLookup);

        from("direct:price-query").routeId("price-lookup")
                .process(BookServices::authorLookup);

        from("direct:author-query").routeId("author-lookup")
                .process(BookServices::priceLookup);
    }


    public static class TypeConverters implements org.apache.camel.TypeConverters {
        @Converter
        public ISBN13 lookupIsbn(final String title, final Exchange exchange) {
            final ISBN13 isbn = BookServices.isbnLookup(title);
            if(isbn == null) {
                throw new IllegalStateException("failed to lookup ISBN code for title '"+title+"'");
            }
            final Properties bookProps = new Properties();
            bookProps.put("isbn", isbn);
            bookProps.put("title",title);
            exchange.setProperty("bookProps", bookProps);
            return isbn;
        }

        @Converter
        public BookRecord buildBook(final Map<String, Object> properties) {
            assert properties.containsKey("isbn");
            assert properties.containsKey("author");
            assert properties.containsKey("published");
            assert properties.containsKey("price");
            assert properties.containsKey("title");
            final ISBN13 bookCode = (ISBN13) properties.get("isbn");
            final String author = (String) properties.get("author");
            final LocalDate published = (LocalDate) properties.get("published");
            final String title = (String) properties.get("title");
            final BigDecimal price = (BigDecimal) properties.get("price");
            return new BookRecord(bookCode, author, published, title, price);
        }
    }

}
