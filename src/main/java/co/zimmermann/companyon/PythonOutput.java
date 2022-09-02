package co.zimmermann.companyon;

import lombok.NonNull;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import de.f0rce.ace.enums.AceTheme;


public class PythonOutput extends HorizontalLayout {

    public PythonOutput(@NonNull final PythonConsole console, @NonNull final String text) {
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

        @NonNull final var closeButton = new Button(VaadinIcon.CLOSE.create());
        closeButton.addClickListener(event -> {
            console.getUI().ifPresent(ui -> ui.access(() -> {
                console.remove(this);
            }));
        });

        this.add(ace);
        this.add(closeButton);
        this.setWidthFull();
    }
}
