package co.zimmermann.companyon;

import lombok.NonNull;

import one.util.streamex.StreamEx;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;


public final class MarkdownCode extends AbstractCode {

    @NonNull
    private static final Parser PARSER = Parser.builder().build();

    @NonNull
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

    @NonNull
    private final Node node;

    public MarkdownCode(@NonNull final Node node) {
        super(node.getChars().toString());
        this.node = node;
    }

    public MarkdownCode(@NonNull final String text) {
        super(text);
        this.node = PARSER.parse(text);
    }

    @NonNull
    public Node[] toNodes() {
        if (this.node instanceof Document document) {
            return StreamEx.of(this.node.getChildren().iterator()).toArray(Node[]::new);
        }

        return new Node[] { this.node };
    }
}
