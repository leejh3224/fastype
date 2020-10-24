package fastype;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

@Slf4j
public class Auth {
    private static Auth auth;
    private static WebDriver driver;

    private Auth() {
        Path chromeDriverPath = Paths.get(System.getProperty("user.home"), "Dropbox/chromedriver");
        System.setProperty("webdriver.chrome.driver", chromeDriverPath.normalize().toString());
        driver = new ChromeDriver();
    }

    public static Auth getInstance() {
        if (auth == null) {
            synchronized (Auth.class) {
                if (auth == null) {
                    auth = new Auth();
                }
            }
        }
        return auth;
    }

    private void open(String url) {
        driver.get(url);
    }

    private Set<Cookie> getCookies() {
        return driver.manage().getCookies();
    }

    public void close() {
        driver.close();
    }

    public String getToken() {
        String postypeUrl = "https://www.postype.com";
        String postypeTokenKey = "PSE1";

        open(postypeUrl + "/login");
        new WebDriverWait(driver, 30).until(
                ExpectedConditions.and(
                        ExpectedConditions.urlToBe(postypeUrl + "/"),
                        ExpectedConditions.titleIs("홈")
                )
        );

        Set<Cookie> cookies = getCookies();
        Map<String, String> cookieMap = CookieHelper.parseMany(cookies);

        log.debug("login successful, closing...");
        driver.close();

        return cookieMap.get(postypeTokenKey);
    }
}
