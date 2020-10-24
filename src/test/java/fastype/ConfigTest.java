package fastype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
    @Test
    @DisplayName("url value should be normalized and saved")
    void name() {
        Config.set("blogUrl", "https://gompro.postype.com/");
        Config.write();
        Config.load();
        String value = Config.get("blogUrl");

        assertEquals(value, "https://gompro.postype.com");
    }
}