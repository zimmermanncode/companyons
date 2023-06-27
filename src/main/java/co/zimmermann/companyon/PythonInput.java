package co.zimmermann.companyon;

import javax.annotation.Nullable;

import lombok.NonNull;

import de.f0rce.ace.enums.AceMode;


public class PythonInput extends AbstractInput {

    @NonNull
    private final PythonThread python;

    public PythonInput(@NonNull final PythonThread python, @Nullable final PythonCode code) {
        super(code, AceMode.python);
        this.python = python;
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
