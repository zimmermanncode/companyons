package co.zimmermann.companyon;

import javax.annotation.Nullable;

import lombok.NonNull;

import de.f0rce.ace.enums.AceMode;


public class PythonInput extends AbstractInput {

    @NonNull
    private final PythonThread python;

    public PythonInput(@NonNull final PythonThread python, @Nullable final PythonCode code) {
        super(python.getConsole(), AceMode.python, code);
        this.python = python;

        super.ace.addFocusListener(event -> {
            super.console.setFocusedPythonAce(event.getSource());
        });

        super.ace.addBlurListener(ignoredEvent -> {
            super.console.setFocusedPythonAce(null);
        });
    }

    @Override
    public void execute() {
        this.python.execute(this, this.getCode());
    }

    @Override
    public @NonNull PythonCode getCode() {
        return new PythonCode(super.toString());
    }
}
