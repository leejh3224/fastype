package fastype;

import lombok.extern.slf4j.Slf4j;
import org.commonmark.Extension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.*;

import java.nio.file.Path;
import java.util.*;

@Slf4j
public class MarkdownRenderer implements PostypeRenderer {
    final List<Extension> extensions = List.of(
            TablesExtension.create(),
            YamlFrontMatterExtension.create()
    );

    Node document;
    Parser parser;

    String markdown;
    List<String> images = new ArrayList<>();

    public MarkdownRenderer(String markdown) {
        this.init(markdown);
    }

    public MarkdownRenderer(Path markdownFile) {
        this.init(FileHelper.readFile(markdownFile));
    }

    private void init(String markdown) {
        parser = Parser
                .builder()
                .extensions(extensions)
                .build();
        this.markdown = markdown;
        this.document = parser.parse(markdown);
    }

    @Override
    public String render() {
        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(extensions)
                .nodeRendererFactory(FencedCodeBlackRenderer::new)
                .nodeRendererFactory(CodeBlockRenderer::new)
                .attributeProviderFactory(attributeProviderContext -> new BlockQuoteAttributeProvider())
                .attributeProviderFactory(attributeProviderContext -> new LinkAttributeProvider())
                .build();

        document.accept(new AbstractVisitor() {
            public void visit(Heading heading) {
                heading.setLevel(heading.getLevel() + 2);
                visitChildren(heading);
            }
        });

        return renderer.render(document);
    }

    @Override
    public List<String> collectImages() {
        document.accept(new AbstractVisitor() {
            public void visit(Image image) {
                images.add(image.getDestination());
            }
        });
        return images;
    }

    @Override
    public Map<String, List<String>> collectFrontMatter() {
        YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
        document.accept(visitor);
        return visitor.getData();
    }

    private static class BlockQuoteAttributeProvider implements AttributeProvider {
        @Override
        public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
            if (node instanceof BlockQuote) {
                attributes.put("class", "blockquote-type2");
            }
        }
    }

    private static class LinkAttributeProvider implements AttributeProvider {
        @Override
        public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
            if (node instanceof Link) {
                attributes.put("rel", "nofollow noopener noreferrer");
                attributes.put("target", "_blank");
            }
        }
    }

    private static class CodeBlockRenderer implements NodeRenderer {

        private final HtmlWriter html;

        CodeBlockRenderer(HtmlNodeRendererContext context) {
            this.html = context.getWriter();
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return Collections.singleton(Code.class);
        }

        @Override
        public void render(Node node) {
            Code code = (Code) node;
            html.tag("span", getSpanAttrs());
            html.tag("em");
            html.text(code.getLiteral());
            html.tag("/em");
            html.tag("/span");
        }

        private Map<String, String> getSpanAttrs() {
            return Map.of(
                "style", "color: rgb(112, 112, 112);"
            );
        }
    }

    private static class FencedCodeBlackRenderer implements NodeRenderer {

        private final HtmlWriter html;

        FencedCodeBlackRenderer(HtmlNodeRendererContext context) {
            this.html = context.getWriter();
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return Collections.singleton(FencedCodeBlock.class);
        }

        @Override
        public void render(Node node) {
            FencedCodeBlock codeBlock = (FencedCodeBlock) node;
            String codeType = toPostypeCodeType(codeBlock.getInfo());
            String prefix = "<div class=\"element-editor-container code\" contenteditable=\"false\"><pre data-type=\"" + codeType + "\">";
            String postfix = "</pre></div>";
            html.raw(prefix);
            html.text(codeBlock.getLiteral());
            html.raw(postfix);
        }

        private String toPostypeCodeType(String lang) {
            switch (lang) {
                case "c":
                    return "text/x-csrc";
                case "csharp":
                case "c#":
                    return "text/x-csharp";
                case "cpp":
                case "c++":
                    return "text/x-c++src";
                case "coffee":
                case "coffeescript":
                    return "text/coffeescript";
                case "Dockerfile":
                    return "text/x-dockerfile";
                case "dart":
                    return "application/dart";
                case "css":
                    return "text/css";
                case "java":
                    return "text/x-java";
                case "kt":
                case "kotlin":
                    return "text/x-kotlin";
                case "json":
                    return "application/json";
                case "html":
                    return "text/html";
                case "md":
                case "markdown":
                    return "text/x-markdown";
                case "hs":
                case "haskell":
                    return "text/x-haskell";
                case "objc":
                case "objective-c":
                    return "text/x-objectivec";
                case "js":
                case "javascript":
                    return "text/javascript";
                case "ts":
                case "typescript":
                    return "text/typescript";
                case "go":
                    return "text/x-go";
                case "php":
                    return "application/x-httpd-php";
                case "py":
                case "python":
                    return "text/x-python";
                case "ps1":
                case "psm1":
                case "powershell":
                    return "application/x-powershell";
                case "pl":
                case "perl":
                    return "text/x-perl";
                case "sc":
                case "scala":
                    return "text/x-scala";
                case "sass":
                    return "text/x-sass";
                case "r":
                    return "text/x-rsrc";
                case "rb":
                case "ruby":
                    return "text/x-ruby";
                case "sh":
                    return "text/x-sh";
                case "vue":
                    return "text/x-vue";
                case "swift":
                    return "text/x-swift";
                case "sql":
                    return "text/x-sql";
                case "xml":
                    return "application/xml";
                default:
                    return "text/plain";
            }
        }
    }
}
