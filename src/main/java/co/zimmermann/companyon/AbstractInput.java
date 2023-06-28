package co.zimmermann.companyon;

import javax.annotation.Nullable;

import lombok.NonNull;

import com.vaadin.flow.component.icon.VaadinIcon;

import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import de.f0rce.ace.enums.AceTheme;


public abstract class AbstractInput extends AbstractInputOutput {

    @NonNull
    protected final AceEditor ace;

    protected AbstractInput(
            @NonNull final PythonConsole console,
            @NonNull final AceMode aceMode,
            @Nullable final AbstractCode code) {

        this(console, aceMode, (code == null) ? null : code.toString());
    }

    protected AbstractInput(
            @NonNull final PythonConsole console,
            @NonNull final AceMode aceMode,
            @Nullable final String text) {

        super(console, null, null);

        @NonNull final var ace = this.ace = new AceEditor(AceTheme.terminal, aceMode, null, null);
        ace.setDisplayIndentGuides(true);
        ace.setWidthFull();
        ace.setWrap(true);

        if (text != null) {
            ace.setValue(text);
        }

        ace.setAutoComplete(true);

        ace.addAceReadyListener(event -> {
            ace.getElement().executeJs("""
                    this.editor.container.style.position = 'relative';
                    this.editor.setOption('maxLines', Infinity);
                    """);
        });

        super.addToolbarButton(VaadinIcon.STEP_FORWARD, event -> {
            this.execute();
        });

        super.addToolbarButton(VaadinIcon.CLOSE, event -> {
            this.delete();
        });

        super.details.setContent(ace);
    }

    @Override
    public void delete() {
        this.console.removeInput(this);
    }

    public abstract void execute();

    @NonNull
    public abstract AbstractCode getCode();

    @NonNull @Override
    public String toString() {
        return this.ace.getValue();
    }
}
