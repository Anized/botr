import org.anized.jafool.CamelApp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AppTest {

    @Test
    @DisplayName("App should start, run and exit")
    public void startApp() throws Exception {
        CamelApp.main(new String[0]);
    }
}
