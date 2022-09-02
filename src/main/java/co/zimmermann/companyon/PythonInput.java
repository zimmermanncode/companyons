package co.zimmermann.companyon;

import javax.annotation.Nullable;

import lombok.NonNull;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import de.f0rce.ace.enums.AceTheme;


public class PythonInput extends HorizontalLayout {


    @NonNull
    private final AceEditor ace;

    @NonNull
    private final PythonConsole.Python python;

    public PythonInput(@NonNull final PythonConsole.Python python, @Nullable final PythonConsole.Code code) {
        this.python = python;

        @NonNull final var ace = this.ace = new AceEditor(AceTheme.terminal, AceMode.python, null, null);
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

    public String getCode() {
        return this.ace.getValue();
    }

    public void execute() {
        this.python.execute(this, this.getCode());
    }
}
