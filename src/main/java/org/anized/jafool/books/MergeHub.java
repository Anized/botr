package org.anized.jafool.books;

import io.vavr.Function2;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import java.util.Optional;
import java.util.Properties;

public class MergeHub implements AggregationStrategy {

    public Exchange aggregate(final Exchange oldExchange, final Exchange newExchange) {
        return Optional.ofNullable(oldExchange).map(exchange ->
                mergeBodies.apply(exchange, newExchange)).orElse(newExchange);
    }

    private static final Function2<Exchange, Exchange, Exchange> mergeBodies = (exch1, exch2) -> {
        exch1.getIn().getBody(Properties.class)
                .putAll(exch2.getIn().getBody(Properties.class));
        return exch1;
    };
}