package co.zimmermann.companyon;

import java.util.Optional;

import lombok.NonNull;

import jep.Jep;
import jep.JepException;


public class PythonCode extends AbstractCode {

    public PythonCode(@NonNull final String text) {
        super(text);
    }

    @NonNull
    public Optional<Object> evaluateWith(@NonNull final Jep interpreter) throws JepException {
        try {
            interpreter.invoke("compile", super.text, "__main__", "eval");
            interpreter.exec("_ = " + super.text);
            return Optional.ofNullable(interpreter.getValue("_"));

        } catch (final JepException e) {
            interpreter.exec(super.text);
            return Optional.empty();
        }
    }
}
