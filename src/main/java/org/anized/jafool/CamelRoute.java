package org.anized.jafool;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConverters;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;

import java.time.ZonedDateTime;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.apache.camel.http.common.HttpMethods.GET;

public class CamelRoute extends RouteBuilder {
    private static final JacksonDataFormat jsonFormat = new JacksonDataFormat();
    private String worldClockUrl;
    public CamelRoute(final String worldClockUrl) {
        this.worldClockUrl = worldClockUrl;
    }

    @Override
    public void configure() {
        getContext().getTypeConverterRegistry()
                .addTypeConverters(new EventTypeConverters());

        from("direct:worldclock").routeId("clock")
                .setHeader(Exchange.HTTP_METHOD).constant(GET)
                .to("log:org.anized?level=DEBUG&showAll=true")
                .toD(worldClockUrl)
                .to("log:org.anized?level=DEBUG&showAll=true")
                .unmarshal(jsonFormat)
                .to("log:org.anized?level=DEBUG&showAll=true")
                .convertBodyTo(ZonedDateTime.class);
    }

    static {
        jsonFormat.disableFeature(FAIL_ON_UNKNOWN_PROPERTIES);
        jsonFormat.setUnmarshalType(DateTimeReport.class);
    }

    public static class EventTypeConverters implements TypeConverters {
        @Converter
        public ZonedDateTime toZonedDateTime(final DateTimeReport report) {
            return ZonedDateTime.parse(report.currentDateTime);
        }
    }
}
