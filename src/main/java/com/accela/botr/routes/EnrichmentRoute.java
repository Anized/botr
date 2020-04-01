package com.accela.botr.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vavr.control.Try;
import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static org.apache.camel.http.common.HttpMethods.GET;

public class EnrichmentRoute extends EndpointRouteBuilder {
    public static final String RESULT_FIELD = "result";
    private final ObjectMapper mapper = new ObjectMapper();
    private final String routeKey;

    public EnrichmentRoute(final String routeKey) {
        this.routeKey = routeKey;
    }

    @Override
    public void configure() {
        from(seda(routeKey)).routeId(routeKey)
                .setProperty("route-key", simple(routeKey))
                .setHeader(Exchange.HTTP_METHOD).constant(GET)
                .toD("${properties:base-url}/${exchangeProperty.record-key}/${exchangeProperty.route-key}?offset=0")
                .process(this::convert);
    }

    private void convert(final Exchange exchange) {
        final JsonNode result = Try.of(() ->
            mapper.readValue(exchange.getIn().getBody(String.class), ObjectNode.class))
                .filter(node -> node.has(RESULT_FIELD))
                .map(node -> node.get(RESULT_FIELD)).get();
        exchange.getMessage().setBody(
            JsonNodeFactory.instance.arrayNode(1)
                .add(JsonNodeFactory.instance.objectNode().set(routeKey, result)), ArrayNode.class);
    }

}
