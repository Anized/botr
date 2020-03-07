package org.anized.jafool;

import java.time.ZonedDateTime;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConverters;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import static org.apache.camel.http.common.HttpMethods.GET;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class CamelRoute extends RouteBuilder {

    @Override
    public void configure() {
        getContext().getTypeConverterRegistry()
                .addTypeConverters(new EventTypeConverters());

        from("direct:worldclock")
                .setHeader(Exchange.HTTP_METHOD).constant(GET)
                .toD("http://worldclockapi.com/api/json/${body}/now")
                .unmarshal(format)
                .to("log:org.anized?level=DEBUG&showAll=true")
                .convertBodyTo(ZonedDateTime.class);
    }

    private static final JacksonDataFormat format = new JacksonDataFormat();
    static {
        format.disableFeature(FAIL_ON_UNKNOWN_PROPERTIES);
        format.setUnmarshalType(DateTimeReport.class);
    }

    public static class EventTypeConverters implements TypeConverters {
        @Converter
        public ZonedDateTime toZonedDateTime(final DateTimeReport report) {
            return ZonedDateTime.parse(report.currentDateTime);
        }
    }
}
