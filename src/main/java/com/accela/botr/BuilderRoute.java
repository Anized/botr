package com.accela.botr;

import com.accela.botr.routes.EnrichmentRoute;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;
import org.apache.camel.builder.ThreadPoolBuilder;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.spi.PropertiesComponent;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

public class BuilderRoute extends EndpointRouteBuilder {
    private final List<String> subRoutes = ImmutableList.<String>builder()
            .add("comments","conditions","contacts","inspections","customForms")
            .build();

    @Override
    public void configure() throws Exception {
        final PropertiesComponent pc = getContext().getPropertiesComponent();
        getContext().getTypeConverterRegistry()
                .addTypeConverters(new DataConverters());
        pc.setLocation("classpath:route.properties");
        final ExecutorService ec =
                new ThreadPoolBuilder(getContext())
                        .poolSize(4)
                        .maxPoolSize(40)
                        .maxQueueSize(20)
                        .build("builder-pool");

        from("direct:build-record")
                .setProperty("record-key", simple("${body}"))
                .setHeader("x-accela-appid").simple("${properties:accela-appid}")
                .setHeader("x-accela-agency").simple("${properties:accela-agency}")
                .setHeader("x-accela-environment").simple("${properties:accela-environment}")
                .setHeader("Authorization").simple("${properties:auth-key}")
                .multicast()
                .parallelProcessing(true)
                .executorService(ec)
                .aggregationStrategy(new MergeHub())
                    .to(seda("comments"))
                    .to(seda("conditions"))
                    .to(seda("contacts"))
                    .to(seda("inspections"))
                    .to(seda("customForms"))
                .end()
                .convertBodyTo(ObjectNode.class);

        for (final String routeName : subRoutes) {
            getContext().addRoutes(new EnrichmentRoute(routeName));
        }
    }

    public static class DataConverters implements TypeConverters {
        @Converter
        public ObjectNode reduceJsonObject(final ArrayNode results) {
            return IntStream.range(0, results.size())
                .mapToObj(i -> (ObjectNode) results.get(i))
                .reduce(JsonNodeFactory.instance.objectNode(), ObjectNode::setAll);
        }
    }

}