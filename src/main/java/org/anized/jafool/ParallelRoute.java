package org.anized.jafool;

import org.anized.jafool.books.BookServices;
import org.anized.jafool.books.MergeHub;
import org.anized.jafool.books.model.BookRecord;
import org.anized.jafool.books.model.ISBN13;
import org.apache.camel.Converter;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ThreadPoolBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

public class ParallelRoute extends RouteBuilder {
    private final BookServices bookServices = new BookServices();

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

        from("direct:book-search")
                .process(bookServices::isbnLookup)
                .multicast()
                .parallelProcessing(true)
                .executorService(ec)
                .aggregationStrategy(new MergeHub())
                    .to("seda:published-query")
                    .to("seda:price-query")
                    .to("seda:author-query")
                .end()
                .convertBodyTo(BookRecord.class);

        from("seda:published-query").routeId("published-lookup")
                .process(bookServices::publishedLookup);

        from("seda:price-query").routeId("price-lookup")
                .process(bookServices::authorLookup);

        from("seda:author-query").routeId("author-lookup")
                .process(bookServices::priceLookup);
    }


    public static class TypeConverters implements org.apache.camel.TypeConverters {
        @Converter
        public BookRecord buildBook(final Properties properties) {
            final ISBN13 bookCode = (ISBN13) properties.get("isbn");
            final String author = (String) properties.get("author");
            final LocalDate published = (LocalDate) properties.get("published");
            final String title = (String) properties.get("title");
            final BigDecimal price = (BigDecimal) properties.get("price");
            return new BookRecord(bookCode, author, published, title, price);
        }
    }

}
