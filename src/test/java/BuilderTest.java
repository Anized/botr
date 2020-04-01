import com.accela.botr.BuilderRoute;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BuilderTest extends CamelTestSupport {

    @Test
    @DisplayName("Request should result in output record being successfully built")
    public void testBookLookupProcess() {
        final JsonNode result = template.requestBody("direct:build-record", "QA-20CAP-00000-000KO", JsonNode.class);
        System.out.println("result:"+result.toPrettyString());
        assertNotNull(result.get("comments"));
        assertNotNull(result.get("conditions"));
        assertNotNull(result.get("contacts"));
        assertNotNull(result.get("inspections"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() throws Exception {
                context.addRoutes(new BuilderRoute());
            }
        };
    }

}