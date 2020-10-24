package fastype;

import org.openqa.selenium.Cookie;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CookieHelper {
    public static Map<String, String> parseMany(Set<Cookie> cookies) {
        Map<String, String> map = new HashMap<>();
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            String value = cookie.getValue();
            map.put(name, value);
        }
        return map;
    }
}
