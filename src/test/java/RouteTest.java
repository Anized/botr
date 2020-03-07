import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RouteTest extends CamelTestSupport {

    @Test
    @DisplayName("Message payload should be transformed")
    public void testMessageTransform() {
        final Endpoint resultEndpoint = resolveMandatoryEndpoint("direct:start");

        final Object result = template.requestBody(resultEndpoint, "Hello World");
        assertEquals("<p>Message is: <b>Hello World</b></p>", result);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start")
                        .transform(simple("<b>${body}</b>"))
                        .to("direct:next");

                from("direct:next")
                        .process(exchange -> {
                            final String output = "<p>Message is: " + exchange.getIn().getBody(String.class) + "</p>";
                            exchange.getMessage().setBody(output);
                        });
            }
        };
    }
}