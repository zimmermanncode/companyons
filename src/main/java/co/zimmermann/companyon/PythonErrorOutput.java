package co.zimmermann.companyon;

import lombok.NonNull;


public class PythonErrorOutput extends PythonOutput {

    public PythonErrorOutput(@NonNull final PythonConsole console, @NonNull final String label, @NonNull final Throwable t) {
        this(console, label, t.getMessage());
    }

    public PythonErrorOutput(
            @NonNull final PythonConsole console,
            @NonNull final String label,
            @NonNull final ErrorOutputBlock block) {

        this(console, label, block.toString());
    }

    public PythonErrorOutput(@NonNull final PythonConsole console, @NonNull final String label, @NonNull final String text) {
        super(console, "error", label, text);
    }
}
