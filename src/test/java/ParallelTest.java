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

import static org.junit.jupiter.api.Assertions.*;

public class ParallelTest extends CamelTestSupport {

    @Test
    @DisplayName("Request should be processed in parallel and an aggregated result returned")
    public void testBookLookupProcess() {
        final BookRecord result = template.requestBody("direct:book-search", "Babylon's Ashes", BookRecord.class);
        assertEquals("ISBN-13:9780316217644", result.getIsbnCode().toString());
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
                template.requestBody("direct:book-search", "Caliban's War", BookRecord.class));
        assertEquals(expectError, Throwables.getRootCause(exception).getMessage());
    }

    @Test
    @DisplayName("Invalid ISBN codes will be rejected")
    public void testInvalidISBNCode() {
        final String expectError = "ISBN code cannot be empty or null and must be prefixed with 'ISBN-13:'";
        final Throwable exception1 = assertThrows(Exception.class, () -> new ISBN13(null));
        assertEquals(expectError, Throwables.getRootCause(exception1).getMessage());
        final Throwable exception2 = assertThrows(Exception.class, () -> new ISBN13(""));
        assertEquals(expectError, Throwables.getRootCause(exception2).getMessage());
        final Throwable exception3 = assertThrows(Exception.class, () -> new ISBN13("9780316217644"));
        assertEquals(expectError, Throwables.getRootCause(exception3).getMessage());
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