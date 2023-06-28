package co.zimmermann.companyon;

import javax.annotation.Nullable;

import lombok.NonNull;

import de.f0rce.ace.enums.AceMode;


public class MarkdownInput extends AbstractInput {

    public MarkdownInput(@NonNull final PythonConsole console, @Nullable final MarkdownCode code) {
        super(console, AceMode.markdown, code);
    }

    public MarkdownInput(@NonNull final PythonConsole console, @Nullable final String text) {
        super(console, AceMode.markdown, text);
    }

    @Override
    public void execute() {
        super.console.replaceInputWithOutput(this, MarkdownOutput.forConsole(super.console).fromCode(this.getCode()));
    }

    @Override
    public @NonNull MarkdownCode getCode() {
        return new MarkdownCode(super.toString());
    }
}
