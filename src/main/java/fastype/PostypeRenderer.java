package fastype;

import java.util.List;
import java.util.Map;

public interface PostypeRenderer {
    String render();
    List<String> collectImages();
    Map<String, List<String>> collectFrontMatter();
}
