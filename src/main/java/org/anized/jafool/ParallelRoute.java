package org.anized.jafool;

import org.anized.jafool.books.LibraryServices;
import org.anized.jafool.books.MergeHub;
import org.anized.jafool.books.model.BookRecord;
import org.anized.jafool.books.model.ISBN13;
import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;
import org.apache.camel.builder.ThreadPoolBuilder;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

public class ParallelRoute extends EndpointRouteBuilder {
    private final LibraryServices library = new LibraryServices();

    @Override
    public void configure() throws Exception {
        final ExecutorService ec =
                new ThreadPoolBuilder(getContext())
                        .poolSize(10)
                        .maxPoolSize(150)
                        .maxQueueSize(150)
                        .build("book-processor-pool");
        getContext().getTypeConverterRegistry()
                .addTypeConverters(new DataConverters());

        from("direct:book-search")
                .process(library::setIsbnCode)
                .multicast()
                .parallelProcessing(true)
                .executorService(ec)
                .aggregationStrategy(new MergeHub())
                    .to(seda("published-query"))
                    .to(seda("price-query"))
                    .to(seda("author-query"))
                .end()
                .convertBodyTo(BookRecord.class);

        from(seda("published-query")).routeId("published-lookup")
                .process(library::publishedLookup);

        from(seda("price-query")).routeId("price-lookup")
                .process(library::authorLookup);

        from(seda("author-query")).routeId("author-lookup")
                .process(library::priceLookup);
    }


    public static class DataConverters implements TypeConverters {
        @Converter
        public BookRecord bookBinder(final Properties properties) {
            return new BookRecord(
                    (ISBN13) properties.get("isbn"),
                    (String) properties.get("author"),
                    (LocalDate) properties.get("published"),
                    (String) properties.get("title"),
                    (BigDecimal) properties.get("price"));
        }
    }

}
