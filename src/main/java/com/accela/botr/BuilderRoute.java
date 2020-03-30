package com.accela.botr;

import com.accela.botr.routes.EnrichmentRoute;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;
import org.apache.camel.builder.ThreadPoolBuilder;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.spi.PropertiesComponent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class BuilderRoute extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        final PropertiesComponent pc = getContext().getPropertiesComponent();
        pc.setLocation("classpath:route.properties");
        final ExecutorService ec =
                new ThreadPoolBuilder(getContext())
                        .poolSize(30)
                        .maxPoolSize(150)
                        .maxQueueSize(150)
                        .build("builder-pool");
        getContext().getTypeConverterRegistry()
                .addTypeConverters(new DataConverters());

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
                .end()
                .convertBodyTo(String.class);

        getContext().addRoutes(new EnrichmentRoute("comments"));
        getContext().addRoutes(new EnrichmentRoute("conditions"));
        getContext().addRoutes(new EnrichmentRoute("contacts"));
        getContext().addRoutes(new EnrichmentRoute("inspections"));
    }


    public static class DataConverters implements TypeConverters {
        @Converter
        public String resulttoJson(final List<ObjectNode> results) {
            final JsonNodeFactory factory = JsonNodeFactory.instance;
            final ObjectNode object = factory.objectNode();
            results.forEach(item -> {
                final Map.Entry<String, JsonNode> head = item.fields().next();
                object.set(head.getKey(),  head.getValue());
            });
            return object.toPrettyString();
        }
    }

}
