package com.accela.botr;

import com.fasterxml.jackson.databind.node.ArrayNode;
import io.vavr.Function2;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import java.util.Optional;

public class MergeHub implements AggregationStrategy {
    public Exchange aggregate(final Exchange oldExchange, final Exchange newExchange) {
        return Optional.ofNullable(oldExchange).map(exchange ->
                mergeBodies.apply(exchange, newExchange)).orElse(newExchange);
    }

    private static final Function2<Exchange, Exchange, Exchange> mergeBodies = (exch1, exch2) -> {
        exch1.getIn().getBody(ArrayNode.class)
                .addAll(exch2.getIn().getBody(ArrayNode.class));
        return exch1;
    };
}