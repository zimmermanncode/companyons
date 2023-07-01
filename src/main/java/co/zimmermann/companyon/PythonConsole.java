package co.zimmermann.companyon;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

import lombok.Getter;
import lombok.NonNull;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import de.f0rce.ace.AceEditor;


public class PythonConsole extends VerticalLayout {

    @NonNull @Getter
    private final Companyon companyon;

    @NonNull
    private final PythonThread python;

    @NonNull
    private AtomicReference<AceEditor> focusedPythonAce = new AtomicReference<>(null);

    @NonNull
    private final Button plusButton = new Button(VaadinIcon.PLUS.create());

    @NonNull
    public List<AbstractInput> getInputs() {
        return super.getChildren().filter(AbstractInput.class::isInstance).map(AbstractInput.class::cast).toList();
    }

    @NonNull
    public List<AbstractInputOutput> getInputsOutputs() {
        return super.getChildren().filter(AbstractInputOutput.class::isInstance).map(AbstractInputOutput.class::cast).toList();
    }

    public PythonConsole(@NonNull final Companyon companyon, @NonNull final List<AbstractBlock> inputScript) {
        this.companyon = companyon;

        @NonNull final var python = this.python = new PythonThread(this);
        python.start();

        if (inputScript.isEmpty()) {
            super.add(new PythonInput(python, null));

        } else {
            for (@NonNull final var block : inputScript) {
                if (block instanceof MarkdownCode markdownCode) {
                    super.add(MarkdownOutput.forConsole(this).fromCode(markdownCode));

                } else if (block instanceof PythonCode pythonCode) {
                    super.add(new PythonInput(python, pythonCode));

                } else if (block instanceof ErrorOutputBlock errorBlock) {
                    super.add(new PythonErrorOutput(this, "", errorBlock));

                } else if (block instanceof OutputBlock outputBlock) {
                    super.add(new PythonOutput(this, "", outputBlock));
                }
            }
        }

        this.plusButton.addClickListener(ignoredEvent -> {
            this.addPythonInputWithInitial(null);
        });

        super.add(this.plusButton);

        super.setSpacing(false);
        super.setWidthFull();
    }

    public void addPythonInputWithInitial(@Nullable final PythonCode code) {
        super.getUI().ifPresent(ui -> ui.access(() -> {
            super.addComponentAtIndex(super.indexOf(this.plusButton), new PythonInput(this.python, code));
        }));
    }

    public void removeInput(@NonNull final AbstractInput input) {
        super.getUI().ifPresent(ui -> ui.access(() -> {
            super.remove(input);
        }));
    }

    public void removeOutput(@NonNull final AbstractOutput output) {
        super.getUI().ifPresent(ui -> ui.access(() -> {
            super.remove(output);
        }));
    }

    public void replaceInputWithOutput(@NonNull final AbstractInput input, @NonNull final AbstractOutput... outputs) {
        super.getUI().ifPresent(ui -> ui.access(() -> {
            @Nonnegative int index = super.indexOf(input);
            super.remove(input);

            for (@NonNull final var output : outputs) {
                super.addComponentAtIndex(index++, output);
            }
        }));
    }

    public void replaceOutputWithInput(@NonNull final AbstractOutput output, @NonNull final AbstractInput input) {
        super.getUI().ifPresent(ui -> ui.access(() -> {
            @Nonnegative final int index = super.indexOf(output);
            super.remove(output);
            super.addComponentAtIndex(index, input);
        }));
    }

    public void executeAll() {
        this.getInputs().forEach(input -> {
            input.execute();
        });
    }

    @NonNull
    public Optional<AceEditor> getFocusedPythonAce() {
        return Optional.ofNullable(this.focusedPythonAce.get());
    }

    public void setFocusedPythonAce(@Nullable final AceEditor ace) {
        this.focusedPythonAce.set(ace);
    }
}
