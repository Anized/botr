package org.anized.jafool.books;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import java.util.Properties;

public class MergeHub implements AggregationStrategy {

    public Exchange aggregate(final Exchange oldExchange, final Exchange newExchange) {
        if(oldExchange != null) {
            oldExchange.getIn().getBody(Properties.class)
                    .putAll(newExchange.getIn().getBody(Properties.class));
           return oldExchange;
        } else {
            return newExchange;
        }
    }

}