package co.zimmermann.companyon;

import lombok.NonNull;

import com.vaadin.flow.component.icon.VaadinIcon;

import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import de.f0rce.ace.enums.AceTheme;


public class PythonOutput extends AbstractOutput {

    @NonNull
    protected final AceEditor ace;

    public PythonOutput(@NonNull final PythonConsole console, @NonNull final String label, @NonNull final OutputBlock block) {
        this(console, label, block.toString());
    }

    public PythonOutput(@NonNull final PythonConsole console, @NonNull final String label, @NonNull final String text) {
        this(console, "success", label, text);
    }

    public PythonOutput(
            @NonNull final PythonConsole console,
            @NonNull final String badgeVariant,
            @NonNull final String label,
            @NonNull final OutputBlock block) {

        this(console, badgeVariant, label, block.toString());
    }

    public PythonOutput(
            @NonNull final PythonConsole console,
            @NonNull final String badgeVariant,
            @NonNull final String label,
            @NonNull final String text) {

        super(console, badgeVariant, label);

        @NonNull final var ace = this.ace = new AceEditor(AceTheme.terminal, AceMode.text, null, null);
        ace.setReadOnly(true);
        ace.setShowGutter(false);
        ace.setValue(text);
        ace.setWidthFull();
        ace.setWrap(false);

        ace.addAceReadyListener(event -> {
            event.getSource().getElement().executeJs("""
                    this.editor.container.style.position = 'relative';
                    this.editor.setOption('maxLines', Infinity);
                    """);
        });

        super.addToolbarButton(VaadinIcon.CLOSE, ignoredEvent -> {
            this.delete();
        });

        super.details.setContent(ace);
    }

    @Override
    public void delete() {
        super.console.removeOutput(this);
    }

    @Override
    public String toString() {
        return this.ace.getValue();
    }
}
