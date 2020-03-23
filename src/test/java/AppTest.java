import org.anized.jafool.CamelApp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AppTest {

    @Test
    @DisplayName("App should start, run and exit")
    public void runningApp() throws Exception {
        CamelApp.main(new String[]{"http://worldclockapi.com/api/json/${body}/now"});
    }

    @Test
    @DisplayName("App will fail if worldclock url is not provided")
    public void failingApp() {
        Assertions.assertThrows(Exception.class,
                () -> CamelApp.main(new String[]{null}));
    }
}
