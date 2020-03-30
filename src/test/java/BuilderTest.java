import com.accela.botr.BuilderRoute;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BuilderTest extends CamelTestSupport {

    @Test
    @DisplayName("Request should result in output record being successfully built")
    public void testBookLookupProcess() {
        final String result = template.requestBody("direct:build-record", "QA-20CAP-00000-000KO", String.class);
        System.out.println("result:"+result);
        final DocumentContext jpath = JsonPath.parse(result);
        assertNotNull(jpath.read("$.comments"));
        assertNotNull(jpath.read("$.conditions"));
        assertNotNull(jpath.read("$.contacts"));
        assertNotNull(jpath.read("$.inspections"));
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