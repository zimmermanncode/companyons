package co.zimmermann.companyon;


import lombok.NonNull;


public abstract class AbstractBlock {

    @NonNull
    protected final String text;

    protected AbstractBlock(@NonNull final String text) {
        this.text = text;
    }

    @NonNull @Override
    public String toString() {
        return this.text;
    }
}
