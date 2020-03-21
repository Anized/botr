import org.anized.jafool.CamelApp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AppTest {

    @Test
    @DisplayName("App should start, run and exit")
    public void runningApp() throws Exception {
        System.setProperty("worldclock.url","http://worldclockapi.com/api/json/${body}/now");
        CamelApp.main(new String[0]);
    }

    @Test
    @DisplayName("App will fail if worldclock url is not set")
    public void failingApp() {
        System.clearProperty("worldclock.url");
        Assertions.assertThrows(Exception.class,
                () -> CamelApp.main(new String[0]));
    }
}
