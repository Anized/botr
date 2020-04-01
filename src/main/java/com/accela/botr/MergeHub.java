package com.accela.botr;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import java.util.Optional;

public class MergeHub implements AggregationStrategy {

    public Exchange aggregate(final Exchange oldExchange, final Exchange newExchange) {
        return Optional.ofNullable(oldExchange).map(exchange -> {
            oldExchange.getIn().getBody(ArrayNode.class)
                    .addAll(newExchange.getIn().getBody(ArrayNode.class));
            return oldExchange;
        }).orElse(newExchange);
    }
}