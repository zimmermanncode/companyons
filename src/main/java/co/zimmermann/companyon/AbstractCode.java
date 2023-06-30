package co.zimmermann.companyon;

import lombok.NonNull;


public abstract class AbstractCode extends AbstractBlock {

    protected AbstractCode(@NonNull final String text) {
        super(text);
    }

}
