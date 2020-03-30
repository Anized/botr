package com.accela.botr.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vavr.control.Try;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.spi.PropertiesComponent;

import java.util.ArrayList;
import java.util.List;

import static org.apache.camel.http.common.HttpMethods.GET;

public class EnrichmentRoute extends EndpointRouteBuilder {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String contextKey;

    public EnrichmentRoute(final String contextKey) {
        this.contextKey = contextKey;
    }

    @Override
    public void configure() {

        from(seda(contextKey)).routeId(contextKey)
                .setProperty("context-key", simple(contextKey))
                .setHeader(Exchange.HTTP_METHOD).constant(GET)
                .toD("${properties:base-url}/${exchangeProperty.record-key}/${exchangeProperty.context-key}?offset=0")
                .process(this::convert);
    }

    private void convert(final Exchange exch) {
        final String json = exch.getIn().getBody(String.class);
        final JsonNode result = Try.of(() ->
                mapper.readValue(json, ObjectNode.class))
                .filter(node -> node.has("result"))
                .map(node -> node.get("result"))
                .get();
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        final ObjectNode object = factory.objectNode();
        final ArrayList<ObjectNode> list = new ArrayList<>();
        list.add(object.set(contextKey, result));
        exch.getMessage().setBody(list, List.class);
    }

}
