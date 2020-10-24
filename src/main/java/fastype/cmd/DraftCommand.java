package fastype.cmd;

import fastype.Config;
import fastype.MarkdownRenderer;
import fastype.PostypeRenderer;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
@Command(
        name = "draft",
        description = {
                "if `id` is not given, new draft will be generated, otherwise, " +
                        "`content` will be written to draft with `id` and if `content` contains images (![alt](link)) then those images will be downloaded as well"
        }
)
public class DraftCommand implements Callable<Integer>  {
    @Option(names = { "--id" }, description = "id of a a draft")
    Integer id;

    @Option(names = { "--content" }, description = "markdown content in string or path to a .md file", defaultValue = "")
    String content;

    @Option(names = { "--title" }, description = "title of draft. If omitted and .md file contains front matter with key `title` then it will be used instead", defaultValue = "제목 없음")
    String title;

    @Option(names = { "--subtitle" }, description = "subtitle of draft. If omitted and .md file contains front matter with key `subtitle` then it will be used instead", defaultValue = "")
    String subtitle;

    @Option(names = { "-ip", "--image-path" }, description = "path to download images")
    String imagePath;

    @Override
    public Integer call() throws Exception {
        log.debug("loading config...");
        Config.load();

        if (id == null) {
            int newDraftId = createNewDraft();
            log.debug("new draft created: checkout https://www.postype.com/edit/" + newDraftId);
        } else {
            if (draftExists(id)) {
                if (content.isEmpty()) {
                    log.debug("`content` is empty!");
                    return 2;
                }

                PostypeRenderer renderer = content.endsWith(".md") ?
                        new MarkdownRenderer(Paths.get(content)) : new MarkdownRenderer(content);

                String post = renderer.render();

                if (title.isEmpty() || subtitle.isEmpty()) {
                    Map<String, List<String>> frontMatter = renderer.collectFrontMatter();

                    if (frontMatter.get("title") != null) {
                        title = frontMatter.get("title").get(0);
                    }

                    if (frontMatter.get("subtitle") != null) {
                        subtitle = frontMatter.get("subtitle").get(0);
                    }
                }

                savePost(id, post, title, subtitle);

                if (imagePath == null) {
                    log.debug("--image-path is empty");
                    return 2;
                }

                List<String> sources = renderer.collectImages();
                downloadImages(imagePath, sources);
            } else {
                log.debug("draft with id {} not exists!", id);
            }
        }

        return 0;
    }

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    private Integer createNewDraft() throws IOException, InterruptedException {
        if (Config.get("token") == null) {
            log.debug("`token` is not set in config! please execute `auth` command first.");
            return -1;
        }

        String blogId = Config.get("blogId");
        if (blogId == null) {
            log.debug("`blogUrl` is not set in config!");
            return -1;
        }

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://www.postype.com/edit?blog_id=" + blogId))
                .GET()
                .header("Cookie", "PSE1=" + Config.get("token"))
                .build();

        HttpResponse<Void> res = client.send(req, HttpResponse.BodyHandlers.discarding());

        String path = res.uri().getPath();

        String[] paths = path.split("/");

        return Integer.parseInt(paths[paths.length-1]);
    }

    private boolean draftExists(int draftId) throws IOException, InterruptedException {
        String blogUrl = Config.get("blogUrl");

        if (blogUrl == null) {
            log.debug("`blogUrl` is not set in config!");
            return false;
        }

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://www.postype.com/edit/" + draftId))
                .GET()
                .build();

        HttpResponse<Void> res = client.send(req, HttpResponse.BodyHandlers.discarding());
        return res.statusCode() != 404 && res.uri().toString().equals("https://www.postype.com/edit/" + draftId);
    }

    private void savePost(int draftId, String body, String title, String subtitle) throws IOException, InterruptedException {
        String blogId = Config.get("blogId");
        String token = Config.get("token");

        if (blogId == null) {
            log.debug("`blogUrl` is not set in config!");
            return;
        }

        if (token == null) {
            log.debug("`token` is not set in config! please execute `auth` command first.");
            return;
        }

        Map<Object, Object> formData = new HashMap<>();
        formData.put("characters_limit", "250000");
        formData.put("post_id", String.valueOf(draftId));
        formData.put("content", body);
        formData.put("blog_id", blogId);
        formData.put("auto_save", 1);
        formData.put("default_font", "font-sans-serif");
        formData.put("default_align", "text-left");
        formData.put("use_indent", 0);
        formData.put("use_p_margin", 1);
        formData.put("title", title);
        formData.put("sub_title", subtitle);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://www.postype.com/api/post/save"))
                .POST(buildFormData(formData))
                .header("Cookie", "PSE1=" + token)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200) {
            log.debug("post saved successfully");
        } else {
            log.debug("failed to save post status: {}", res.statusCode());
        }
    }

    public static HttpRequest.BodyPublisher buildFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    public void downloadImages(String dest, List<String> sources) throws IOException {
        Path folder = Paths.get(dest);
        Files.createDirectories(folder);

        for (String source : sources) {
            String path = new URL(source).getPath();
            String[] split = path.split("/");
            String imageName = split[split.length-1];

            try (InputStream in = new URL(source).openStream()) {
                Path dir = Paths.get(folder.toString() + "/" + RandomStringUtils.randomAlphanumeric(8) + "_" + imageName);

                if (Files.notExists(dir)) {
                    Files.copy(in, dir);
                    log.debug("download completed: {}", dir);
                } else {
                    log.debug("file {} already exists, skipping...", dir);
                }
            }
        }

        Runtime.getRuntime().exec("open " + folder);
    }
}
