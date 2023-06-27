package co.zimmermann.companyon;

import javax.annotation.Nullable;

import lombok.NonNull;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import de.f0rce.ace.enums.AceTheme;

public abstract class AbstractInput extends HorizontalLayout {

    @NonNull
    private final AceEditor ace;

    protected AbstractInput(@Nullable final AbstractCode code, @NonNull final AceMode aceMode) {
        @NonNull final var ace = this.ace = new AceEditor(AceTheme.terminal, aceMode, null, null);
        ace.setDisplayIndentGuides(true);
        ace.setWidthFull();
        ace.setWrap(true);

        if (code != null) {
            ace.setValue(code.toString());
        }

        ace.setAutoComplete(true);

        ace.addAceReadyListener(event -> {
            ace.getElement().executeJs("""
                    this.editor.container.style.position = 'relative';
                    this.editor.setOption('maxLines', Infinity);
                    """);
        });

        @NonNull final var execButton = new Button(VaadinIcon.STEP_FORWARD.create());
        execButton.addClickListener(event -> {
            this.execute();
        });

        this.add(ace);
        this.add(execButton);
        this.setWidthFull();
    }

    public abstract void execute();

    @NonNull
    public abstract AbstractCode getCode();

    @NonNull @Override
    public String toString() {
        return this.ace.getValue();
    }
}
