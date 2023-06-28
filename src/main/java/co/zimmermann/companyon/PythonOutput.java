package co.zimmermann.companyon;

import lombok.NonNull;

import com.vaadin.flow.component.icon.VaadinIcon;

import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import de.f0rce.ace.enums.AceTheme;


public class PythonOutput extends AbstractOutput {

    public PythonOutput(@NonNull final PythonConsole console, @NonNull final String label, @NonNull final String text) {
        this(console, "success", label, text);
    }

    public PythonOutput(
            @NonNull final PythonConsole console,
            @NonNull final String badgeVariant,
            @NonNull final String label,
            @NonNull final String text) {

        super(console, badgeVariant, label);

        @NonNull final var ace = new AceEditor(AceTheme.terminal, AceMode.text, null, null);
        ace.setReadOnly(true);
        ace.setShowGutter(false);
        ace.setValue(text);
        ace.setWidthFull();
        ace.setWrap(true);

        ace.addAceReadyListener(event -> {
            ace.getElement().executeJs("""
                    this.editor.container.style.position = 'relative';
                    this.editor.setOption('maxLines', Infinity);
                    """);
        });

        super.addToolbarButton(VaadinIcon.CLOSE, event -> {
            this.delete();
        });

        super.details.setContent(ace);
    }

    @Override
    public void delete() {
        super.console.removeOutput(this);
    }
}
