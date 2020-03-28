package org.anized.jafool.books;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import java.util.Optional;
import java.util.Properties;

public class MergeHub implements AggregationStrategy {

    public Exchange aggregate(final Exchange oldExchange, final Exchange newExchange) {
        return Optional.ofNullable(oldExchange).map(exchange -> {
            exchange.getIn().getBody(Properties.class)
                    .putAll(newExchange.getIn().getBody(Properties.class));
            return exchange;
        }).orElse(newExchange);
    }

}