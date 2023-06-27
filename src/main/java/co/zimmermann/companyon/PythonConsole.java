package co.zimmermann.companyon;

import java.util.List;

import javax.annotation.Nullable;

import lombok.Getter;
import lombok.NonNull;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;


public class PythonConsole extends VerticalLayout {

    @NonNull @Getter
    private final Companyon companyon;

    @NonNull
    private final PythonThread python;

    @NonNull
    private final Button plusButton = new Button(VaadinIcon.PLUS.create());

    @NonNull
    public List<AbstractInput> getInputs() {
        return super.getChildren().filter(AbstractInput.class::isInstance).map(AbstractInput.class::cast).toList();
    }

    public PythonConsole(@NonNull final Companyon companyon, @NonNull final List<AbstractCode> inputScript) {
        this.companyon = companyon;

        @NonNull final var python = this.python = new PythonThread(this);
        python.start();

        if (inputScript.isEmpty()) {
            super.add(new PythonInput(python, null));

        } else {
            for (@NonNull final var code : inputScript) {
                if (code instanceof MarkdownCode markdownCode) {
                    super.add(markdownCode.toHtml());

                } else if (code instanceof PythonCode pythonCode) {
                    super.add(new PythonInput(python, pythonCode));
                }
            }
        }

        this.plusButton.addClickListener(ignoredEvent -> {
            this.addPythonInputFrom(null);
        });

        super.add(this.plusButton);
    }

    public void addPythonInputFrom(@Nullable final PythonCode code) {
        super.getUI().ifPresent(ui -> ui.access(() -> {
            super.addComponentAtIndex(super.indexOf(this.plusButton), new PythonInput(this.python, code));
        }));
    }

    public void executeAll() {
        this.getInputs().forEach(input -> {
            input.execute();
        });
    }
}
