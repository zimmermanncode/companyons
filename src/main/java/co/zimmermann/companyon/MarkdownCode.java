package co.zimmermann.companyon;

import lombok.NonNull;

import com.vaadin.flow.component.Html;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.ast.Node;


public final class MarkdownCode extends AbstractCode {

    @NonNull
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

    @NonNull
    private final Node node;

    public MarkdownCode(@NonNull final Node node) {
        super(node.toString());
        this.node = node;
    }

    @NonNull
    public Html toHtml() {
        return new Html(HTML_RENDERER.render(this.node));
    }
}
