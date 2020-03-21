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

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("direct:fakeResponse")
                        .setBody(responseJson);

                context.addRoutes(new CamelRoute("direct:fakeResponse"));
            }
        };
    }

    private final Function<Exchange,String> responseJson = (Exchange exchange) -> {
        final Map<String,Object> response = new HashMap<>();
        response.put("$id", "1");
        response.put("currentDateTime","2020-03-20T13:15+01:00");
        response.put("utcOffset","01:00:00");
        response.put("isDayLightSavingsTime",false);
        response.put("dayOfTheWeek","Friday");
        response.put("timeZoneName","Central Europe Standard Time");
        response.put("currentFileTime",132291832842915160L);
        response.put("ordinalDate","2020-80");
        response.put("serviceResponse",null);
        try {
            return new ObjectMapper().writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    };
}