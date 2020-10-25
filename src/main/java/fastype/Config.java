package fastype;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Config {
    private static final Path configDir = Path.of(System.getProperty("user.home") + "/.config/fastype");
    private static final File configFile = new File(configDir.normalize().toString() + "/config.yaml");
    private static Map<String, String> config = new HashMap<>();
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public static void write() {
        try {
            Files.createDirectories(configDir);
            load();
            yamlMapper.writeValue(configFile, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        try {
            Map content = yamlMapper.readValue(configFile, Map.class);
            ((Map<String, String>) content).forEach(config::putIfAbsent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        load();
        return config.get(key);
    }

    public static void set(String key, String value) {
        try {
            if (key.equals("blogUrl")) {
                URL url = new URL(value);
                String normalizedUrl = url.getProtocol() + "://" + url.getHost();

                // normalize url
                normalizedUrl = normalizedUrl.replaceAll("/$", "");

                int blogId = getBlogId(normalizedUrl);

                if (blogId == -1) {
                    return;
                }

                config.put("blogId", String.valueOf(blogId));
                config.put("blogUrl", normalizedUrl);
            } else {
                config.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getBlogId(String blogUrl) throws IOException {
        try {
            Document doc = Jsoup.connect(blogUrl).get();
            Element body = doc.select("body").first();
            String blogId = body.attr("data-blog-id");
            return Integer.parseInt(blogId);
        } catch (UnknownHostException unknownHostException) {
            log.debug("url {} contains invalid hostname", blogUrl);
        } catch (HttpStatusException httpStatusException) {
            log.debug("url {} doesn't exists", blogUrl);
        }
        return -1;
    }
}


