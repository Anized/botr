import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.anized.jafool.CamelRoute;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RouteTest extends CamelTestSupport {

    @Test
    @DisplayName("Endpoint should be queried and response transformed")
    public void testMessageTransform() {
        final ZonedDateTime result = template.requestBody("direct:worldclock", "CET", ZonedDateTime.class);
        assertEquals("2020-03-20T13:15+01:00", result.toString());
    }

    final Function<Exchange,String> responseBody = (Exchange exchange) -> {
        final Map<String,Object> payload = new HashMap<>();
        payload.put("$id", "1");
        payload.put("currentDateTime","2020-03-20T13:15+01:00");
        payload.put("utcOffset","01:00:00");
        payload.put("isDayLightSavingsTime",false);
        payload.put("dayOfTheWeek","Friday");
        payload.put("timeZoneName","Central Europe Standard Time");
        payload.put("currentFileTime",132291832842915160L);
        payload.put("ordinalDate","2020-80");
        payload.put("serviceResponse",null);
        try {
            return new ObjectMapper().writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    };


    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("direct:fakeResponse")
                        .setBody(responseBody);

                context.addRoutes(new CamelRoute("direct:fakeResponse"));
            }
        };
    }
}