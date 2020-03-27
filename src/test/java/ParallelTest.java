import com.google.common.base.Throwables;
import org.anized.jafool.books.model.BookRecord;
import org.anized.jafool.ParallelRoute;
import org.anized.jafool.books.model.ISBN13;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParallelTest extends CamelTestSupport {

    @Test
    @DisplayName("Request should be processed in parallel and an aggregated result returned")
    public void testBookLookupProcess() {
        final BookRecord result = template.requestBody("direct:parallel", "Babylon's Ashes", BookRecord.class);
        assertEquals(new ISBN13("ISBN-13:9-780-316-217-644"), result.getIsbnCode());
        assertEquals("S.A. Corey", result.getAuthor());
        assertEquals(BigDecimal.valueOf(3.71), result.getPrice());
        assertEquals("Babylon's Ashes", result.getTitle());
        assertEquals(LocalDate.parse("2016-12-01"), result.getPublished());
    }

    @Test
    @DisplayName("When a service call fails, this should return a meaningful exception to the client")
    public void testInvalidServiceResult() {
        final String expectError = "failed to lookup ISBN code for title 'Caliban's War'";
        final Throwable exception = assertThrows(Exception.class, () ->
                template.requestBody("direct:parallel", "Caliban's War", BookRecord.class));
        assertEquals(expectError, Throwables.getRootCause(exception).getMessage());
    }

    @Test
    @DisplayName("Invalid ISBN codes will be rejected")
    public void testInvalidISBNCode() {
        final String expectError = "ISBN code cannot be empty or null and must be prefixed with 'ISBN-13:'";
        final Throwable exception = assertThrows(Exception.class, () ->
                new ISBN13("9780316217644"));
        assertEquals(expectError, Throwables.getRootCause(exception).getMessage());
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() throws Exception {
                context.addRoutes(new ParallelRoute());
            }
        };
    }

}