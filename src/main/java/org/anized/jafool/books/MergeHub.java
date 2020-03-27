package org.anized.jafool.books;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import java.util.Map;
import java.util.Properties;

public class MergeHub implements AggregationStrategy {

    public Exchange aggregate(final Exchange exchange, final Exchange other) {
        final Exchange result = exchange != null ? exchange : other;
        final Properties bookProps = result.getProperty("bookProps", Properties.class);
        result.getMessage().setBody(bookProps, Map.class);
        return result;
    }

}