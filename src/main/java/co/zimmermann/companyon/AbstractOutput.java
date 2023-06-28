package co.zimmermann.companyon;

import javax.annotation.Nullable;

import lombok.NonNull;


public abstract class AbstractOutput extends AbstractInputOutput {

    protected AbstractOutput(
            @NonNull final PythonConsole console,
            @Nullable final String badgeVariant,
            @Nullable final String label) {

        super(console, badgeVariant, label);
    }

    @Override
    public void delete() {
        super.console.removeOutput(this);
    }
}
