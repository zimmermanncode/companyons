package co.zimmermann.companyon;

import lombok.NonNull;

import one.util.streamex.StreamEx;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.ast.Node;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.icon.VaadinIcon;


public class MarkdownOutput extends AbstractOutput {

    @NonNull
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

    @NonNull
    private final String markdownText;

    @NonNull
    public static Factory forConsole(@NonNull final PythonConsole console) {
        return new Factory(console);
    }

    public static class Factory {

        @NonNull
        private final PythonConsole console;

        private Factory(@NonNull final PythonConsole console) {
            this.console = console;
        }

        public MarkdownOutput[] fromCode(@NonNull final MarkdownCode code) {
            return StreamEx.of(code.toNodes()).map(node -> new MarkdownOutput(this.console, node.getChars().toString(), node))
                    .toArray(MarkdownOutput[]::new);
        }
    }

    private MarkdownOutput(@NonNull final PythonConsole console, @NonNull final String text, @NonNull final Node node) {
        super(console, null, null);

        this.markdownText = text;
        super.details.addContent(new Html(HTML_RENDERER.render(node)));

        super.addToolbarButton(VaadinIcon.STEP_BACKWARD, event -> {
            super.console.replaceOutputWithInput(this, new MarkdownInput(super.console, this.markdownText));
        });

        super.addToolbarButton(VaadinIcon.CLOSE, event -> {
            super.console.removeOutput(this);
        });
    }
}
