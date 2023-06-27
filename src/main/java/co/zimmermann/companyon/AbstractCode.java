package co.zimmermann.companyon;

import lombok.NonNull;

public class AbstractCode {

    @NonNull
    protected final String text;

    public AbstractCode(@NonNull final String text) {
        this.text = text;
    }

    @NonNull
    @Override
    public String toString() {
        return this.text;
    }
}
